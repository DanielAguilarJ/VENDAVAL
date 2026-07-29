// ==========================================================================
// VENDAVAL · Espacios culturales y clima de Aguascalientes
// Lógica de interfaz: directorio, mapa, asistente climático, altas y avisos.
// JavaScript estándar, sin framework. Leaflet y Supabase llegan del HTML.
// Contrato de clases y de ids: index.html + styles.css.
// ==========================================================================

// ==========================================================================
// 0. UTILIDADES
// ==========================================================================

// Símbolos disponibles en el sprite de index.html
const SIMBOLOS = new Set([
  "termometro", "gota", "sol", "viento", "fuego", "ubicacion", "busqueda",
  "sin-resultados", "filtro", "mapa", "capas", "cerrar", "menu", "mas",
  "flecha-derecha", "flecha-arriba", "flecha-abajo", "arbol", "aire", "agua",
  "reloj", "alerta", "info", "telefono", "enlace-externo", "equipo",
  "brujula", "edificio", "nube-sol", "sincronizar", "hoja"
]);

// Único generador de iconos: devuelve el marcado del sprite, nunca un emoji.
function icono(nombre, clases) {
  const simbolo = SIMBOLOS.has(nombre) ? nombre : "info";
  const extra = clases ? " " + clases : "";
  return '<svg class="icon' + extra + '" aria-hidden="true" focusable="false">' +
    '<use href="#icono-' + simbolo + '"></use></svg>';
}

const ESCAPES = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" };

// Todo dato externo (base remota, lista de Google Maps, formulario) se escapa
// antes de entrar en una plantilla de innerHTML.
function esc(valor) {
  if (valor === null || valor === undefined) return "";
  return String(valor).replace(/[&<>"']/g, (c) => ESCAPES[c]);
}

function nodo(etiqueta, clase, texto) {
  const elemento = document.createElement(etiqueta);
  if (clase) elemento.className = clase;
  if (texto !== undefined && texto !== null) elemento.textContent = String(texto);
  return elemento;
}

// Pastilla con icono. El espaciador solo hace falta en contenedores flex sin
// gap declarado (.space-badge, .uv-tag, .overlay-badge).
function pastilla(clase, simbolo, texto, espaciador) {
  const elemento = nodo("span", clase);
  elemento.innerHTML = icono(simbolo) + (espaciador ? "&#160;" : "") + esc(texto);
  return elemento;
}

// Los colores de Leaflet se escriben como atributos SVG y no admiten var(),
// así que se leen del único origen de tokens: el bloque :root de styles.css.
const cacheTokens = new Map();

function colorToken(nombre) {
  if (cacheTokens.has(nombre)) return cacheTokens.get(nombre);
  let valor = "";
  try {
    valor = getComputedStyle(document.documentElement).getPropertyValue(nombre).trim();
  } catch (err) {
    valor = "";
  }
  // Respaldo sin literal de color: currentColor resuelve al color heredado
  // del contenedor del mapa si el token no se pudo leer.
  const resultado = valor || "currentColor";
  cacheTokens.set(nombre, resultado);
  return resultado;
}

function movimientoReducido() {
  return typeof window.matchMedia === "function" &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

// Desplazamiento animado salvo con movimiento reducido (Requisito 2.6).
function irASeccion(elemento) {
  if (!elemento || typeof elemento.scrollIntoView !== "function") return;
  elemento.scrollIntoView({
    behavior: movimientoReducido() ? "auto" : "smooth",
    block: "start"
  });
}

const $ = (id) => document.getElementById(id);

// ==========================================================================
// 1. AVISOS VISIBLES (contenedor #toast-container de index.html)
// ==========================================================================

const TOAST_MAXIMO = 3;
const TOAST_DURACION = 5200;
const TOAST_SALIDA = 400;
const avisosRecientes = new Map();

const VARIANTE_AVISO = {
  info: "",
  exito: "sync-toast-exito",
  alerta: "sync-toast-alerta",
  error: "sync-toast-error"
};

function avisar(opciones) {
  const contenedor = $("toast-container");
  if (!contenedor) return;

  const {
    titulo,
    cuerpo = "",
    tipo = "info",
    simbolo = "info",
    clave = null,
    silenciarMs = 30000
  } = opciones || {};
  if (!titulo) return;

  // Evita repetir el mismo aviso en cada ciclo de sincronización.
  if (clave) {
    const ultimo = avisosRecientes.get(clave) || 0;
    if (Date.now() - ultimo < silenciarMs) return;
    avisosRecientes.set(clave, Date.now());
  }

  while (contenedor.children.length >= TOAST_MAXIMO) {
    contenedor.removeChild(contenedor.firstElementChild);
  }

  const variante = VARIANTE_AVISO[tipo] || "";
  const aviso = nodo("div", variante ? "sync-toast " + variante : "sync-toast");

  const marca = nodo("span", "sync-toast-icon");
  marca.innerHTML = icono(simbolo, "icon-lg");
  aviso.appendChild(marca);

  const contenido = nodo("div", "sync-toast-content");
  contenido.appendChild(nodo("p", "sync-toast-title", titulo));
  if (cuerpo) contenido.appendChild(nodo("p", "sync-toast-body", cuerpo));
  aviso.appendChild(contenido);

  contenedor.appendChild(aviso);

  setTimeout(() => {
    aviso.classList.add("fade-out");
    setTimeout(() => aviso.remove(), TOAST_SALIDA);
  }, TOAST_DURACION);
}

// ==========================================================================
// 2. ESTADO DE RED Y PETICIONES CON TIEMPO LÍMITE (Requisitos 3.7 y 10.7)
// ==========================================================================

const TIEMPO_LIMITE = 10000;
const reintentosPendientes = new Map();

function enLinea() {
  return typeof navigator.onLine === "boolean" ? navigator.onLine : true;
}

// Los reintentos remotos esperan a que vuelva la conexión.
function aplazarHastaConexion(clave, tarea) {
  reintentosPendientes.set(clave, tarea);
}

function ejecutarReintentos() {
  const tareas = Array.from(reintentosPendientes.entries());
  reintentosPendientes.clear();
  tareas.forEach(([clave, tarea]) => {
    try {
      tarea();
    } catch (err) {
      aplazarHastaConexion(clave, tarea);
    }
  });
}

// fetch con AbortController y 10 s de límite.
async function pedir(url, opciones) {
  const controlador = new AbortController();
  const reloj = setTimeout(() => controlador.abort(), TIEMPO_LIMITE);
  try {
    const respuesta = await fetch(url, Object.assign({}, opciones, { signal: controlador.signal }));
    if (!respuesta.ok) throw new Error("HTTP " + respuesta.status);
    return respuesta;
  } finally {
    clearTimeout(reloj);
  }
}

// Límite de tiempo para promesas que no exponen señal de aborto.
function conLimite(promesa, ms) {
  const espera = typeof ms === "number" ? ms : TIEMPO_LIMITE;
  return new Promise((resolver, rechazar) => {
    const reloj = setTimeout(() => rechazar(new Error("Tiempo de espera agotado")), espera);
    Promise.resolve(promesa).then(
      (valor) => { clearTimeout(reloj); resolver(valor); },
      (error) => { clearTimeout(reloj); rechazar(error); }
    );
  });
}

function avisarFuenteCaida(fuente, clave) {
  avisar({
    titulo: "Datos incompletos",
    cuerpo: "No pudimos consultar " + fuente + ". Se muestra el catálogo local disponible y puede estar incompleto.",
    tipo: "alerta",
    simbolo: "alerta",
    clave: clave
  });
}

// ==========================================================================
// 3. ALMACENAMIENTO LOCAL DEFENSIVO (Requisito 10.8)
// ==========================================================================

const CLAVE_RESPALDO = "vendaval_custom_spaces";

function leerAlmacen(clave) {
  try {
    return window.localStorage ? window.localStorage.getItem(clave) : null;
  } catch (err) {
    return null;
  }
}

function escribirAlmacen(clave, valor) {
  try {
    if (!window.localStorage) return false;
    window.localStorage.setItem(clave, valor);
    return true;
  } catch (err) {
    return false;
  }
}

// ==========================================================================
// 4. CATÁLOGO BASE DE ESPACIOS PÚBLICOS CULTURALES
// ==========================================================================

const spacesData = [
  // Municipio: Aguascalientes
  {
    id: 1,
    name: "Instituto Cultural de Aguascalientes",
    address: "Venustiano Carranza 101, Centro, 20000 Aguascalientes, Ags.",
    phone: "449 910 2010",
    type: "Casa de Cultura",
    municipio: "Aguascalientes",
    lat: 21.8804,
    lng: -102.2965,
    mapQuery: "Instituto+Cultural+de+Aguascalientes",
    hasWater: true,
    waterType: "Bebederos públicos de agua potable filtrada en patio central",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Patio colonial arbolado con sombra densa, vegetación y bebederos de agua fresca."
  },
  {
    id: 2,
    name: "Centro Cultural Los Arquitos",
    address: "Alameda S/N, Barrio de la Purísima, 20000 Aguascalientes, Ags.",
    phone: "449 916 9201",
    type: "Casa de Cultura",
    municipio: "Aguascalientes",
    lat: 21.8845,
    lng: -102.2818,
    mapQuery: "Centro+Cultural+Los+Arquitos+Aguascalientes",
    hasWater: true,
    waterType: "Bebederos públicos e hidrantes en corredores",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Antiguos baños térmicos con frondosos jardines, fuentes de agua y sombra arbolada."
  },
  {
    id: 3,
    name: "Casa Terán",
    address: "Rivero y Gutiérrez 110, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 915 1741",
    type: "Casa de Cultura",
    municipio: "Aguascalientes",
    lat: 21.8827,
    lng: -102.2974,
    mapQuery: "Casa+Teran+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua fría en patio principal",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Casona histórica sombreada con librería, cafetería y dispensador de agua potable."
  },
  {
    id: 4,
    name: "Centro Cultural Ángel",
    address: "Rivero y Gutiérrez 110, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 915 1741",
    type: "Casa de Cultura",
    municipio: "Aguascalientes",
    lat: 21.8828,
    lng: -102.2975,
    mapQuery: "Centro+Cultural+Angel+Aguascalientes",
    hasWater: true,
    waterType: "Garrafón de agua potable para visitantes",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Instalaciones de muros gruesos protegidas del sol con aire acondicionado."
  },
  {
    id: 5,
    name: "Centro de Artes Visuales",
    address: "Venustiano Carranza 111, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 916 1456",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8805,
    lng: -102.2970,
    mapQuery: "Centro+de+Artes+Visuales+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua en área de talleres",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Galerías climatizadas y talleres protegidos del calor directo."
  },
  {
    id: 6,
    name: "Artefacto / Espacio Experimental",
    address: "Av. Francisco I. Madero 457, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 125 6259",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8831,
    lng: -102.2928,
    mapQuery: "Artefacto+Espacio+Experimental+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua para asistentes",
    hasAC: false,
    shadeLevel: "Media",
    climateComfort: "Foro independiente con ventilación cruzada y refresco de agua."
  },
  {
    id: 7,
    name: "Casa Refugio Reyes",
    address: "C. Juan de Montoro 423, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 929 4201",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8833,
    lng: -102.2920,
    mapQuery: "Casa+Refugio+Reyes+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero en patio interior",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Casona de cantera diseñada por Refugio Reyes con patio sombreado y fresco."
  },
  {
    id: 8,
    name: 'Galería de Arte "El Obraje"',
    address: "C. Juan de Montoro 222, Centro, 20000 Aguascalientes, Ags.",
    phone: "449 994 0074",
    type: "Galería de Arte",
    municipio: "Aguascalientes",
    lat: 21.8833,
    lng: -102.2950,
    mapQuery: "Galeria+de+Arte+El+Obraje+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua purificada",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Galería con aire acondicionado y resguardo térmico en el centro."
  },
  {
    id: 9,
    name: "Teatro Morelos",
    address: "Nieto 113, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 915 1941",
    type: "Teatro",
    municipio: "Aguascalientes",
    lat: 21.8806,
    lng: -102.2961,
    mapQuery: "Teatro+Morelos+Aguascalientes",
    hasWater: true,
    waterType: "Dispensadores de agua potable en vestíbulo",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Teatro histórico con aire acondicionado central (HVAC) en todas las salas."
  },
  {
    id: 10,
    name: "Teatro Antonio Leal y Romero",
    address: "Venustiano Carranza, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 910 2010",
    type: "Teatro",
    municipio: "Aguascalientes",
    lat: 21.8804,
    lng: -102.2964,
    mapQuery: "Teatro+Antonio+Leal+y+Romero+Aguascalientes",
    hasWater: true,
    waterType: "Acceso a bebederos del complejo cultural",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Sala de teatro completamente climatizada y aislada del sol directo."
  },
  {
    id: 11,
    name: "Galería Benjamín Manzo",
    address: "Venustiano Carranza 101, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 915 1691",
    type: "Galería de Arte",
    municipio: "Aguascalientes",
    lat: 21.8804,
    lng: -102.2965,
    mapQuery: "Galeria+Benjamin+Manzo+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua en sala",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Galería interior con control de clima y sombra del claustro."
  },
  {
    id: 12,
    name: "Teatro Aguascalientes",
    address: "Av. José María Chávez S/N, Jardines de la Asunción, 20270 Aguascalientes, Ags.",
    phone: "449 978 5414",
    type: "Teatro",
    municipio: "Aguascalientes",
    lat: 21.8601,
    lng: -102.2905,
    mapQuery: "Teatro+Aguascalientes",
    hasWater: true,
    waterType: "Estaciones de hidratación en lobby principal",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Recinto de gran escala con climatización central integral y amplia explanada sombreada."
  },
  {
    id: 13,
    name: "Centro CRECER Obraje",
    address: "Prof. Enrique Olivares Santana 119, Obraje, 20230 Aguascalientes, Ags.",
    phone: "449 910 2121 ext. 4255",
    type: "Centro Comunitario",
    municipio: "Aguascalientes",
    lat: 21.8745,
    lng: -102.2910,
    mapQuery: "Centro+CRECER+Obraje+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero público comunitario",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Centro social comunal con patio arbolado y toma de agua potable."
  },
  // Municipio: San José de Gracia
  {
    id: 14,
    name: "Casa de la Cultura de San José de Gracia (ICA)",
    address: "Juan Domínguez 214, Col. Las Cañadas, 20500 San José de Gracia, Ags.",
    phone: "465 967 3436",
    type: "Casa de Cultura",
    municipio: "San José de Gracia",
    lat: 22.1506,
    lng: -102.4158,
    mapQuery: "Casa+de+la+Cultura+San+Jose+de+Gracia+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero escolar y público",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Patio sombreado cerca de la presa con brisa natural y agua fresca."
  },
  {
    id: 15,
    name: "Casa del Peregrino",
    address: "San José de Gracia, Ags.",
    phone: "No disponible",
    type: "Casa de Cultura",
    municipio: "San José de Gracia",
    lat: 22.1520,
    lng: -102.4160,
    mapQuery: "Casa+del+Peregrino+San+Jose+de+Gracia+Aguascalientes",
    hasWater: true,
    waterType: "Toma de agua potable para peregrinos",
    hasAC: false,
    shadeLevel: "Media",
    climateComfort: "Refugio techado para visitantes del Cristo Redentor."
  },
  // Municipio: San Francisco de los Romo
  {
    id: 16,
    name: "Casa de Cultura de San Francisco de los Romo",
    address: "Juárez 410, Centro, 20300 San Francisco de los Romo, Ags.",
    phone: "465 967 0427",
    type: "Casa de Cultura",
    municipio: "San Francisco de los Romo",
    lat: 22.0738,
    lng: -102.2710,
    mapQuery: "Casa+de+Cultura+San+Francisco+de+los+Romo",
    hasWater: true,
    waterType: "Bebedero de agua purificada",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Espacio cultural con áreas verdes y punto de agua potable."
  },
  // Municipio: Rincón de Romos
  {
    id: 17,
    name: "Casa de la Cultura de Rincón de Romos",
    address: "Dolores Hidalgo 309, Centro Histórico, 20400 Rincón de Romos, Ags.",
    phone: "465 951 1482",
    type: "Casa de Cultura",
    municipio: "Rincón de Romos",
    lat: 22.2289,
    lng: -102.3241,
    mapQuery: "Casa+de+la+Cultura+Rincon+de+Romos",
    hasWater: true,
    waterType: "Dispensador de agua fría en recepción",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Patio histórico con sombra de frondosos árboles y bebedero."
  },
  // Municipio: Jesús María
  {
    id: 18,
    name: "Casa de la Cultura Jesús María",
    address: "Ignacio Allende 707, Ayuntamiento, 20920 Jesús María, Ags.",
    phone: "449 965 0017",
    type: "Casa de Cultura",
    municipio: "Jesús María",
    lat: 21.9612,
    lng: -102.3435,
    mapQuery: "Casa+de+la+Cultura+Jesus+Maria+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua para estudiantes y visitantes",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Aulas climatizadas y patio sombreado en la zona centro de Jesús María."
  },
  // Municipio: Calvillo
  {
    id: 19,
    name: "Casa de Cultura de Calvillo",
    address: "C. Zaragoza, Zona Centro, 20800 Calvillo, Ags.",
    phone: "495 956 0807",
    type: "Casa de Cultura",
    municipio: "Calvillo",
    lat: 21.8465,
    lng: -102.7188,
    mapQuery: "Casa+de+Cultura+Calvillo+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero de agua fresca en patio",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Casona colonial sombreada en Calvillo con fuente y agua fresca."
  },
  // Municipio: Pabellón de Arteaga
  {
    id: 20,
    name: "Casa de la Cultura de Pabellón de Arteaga",
    address: "Centro, Pabellón de Arteaga, Ags.",
    phone: "No disponible",
    type: "Casa de Cultura",
    municipio: "Pabellón de Arteaga",
    lat: 22.1415,
    lng: -102.2762,
    mapQuery: "Casa+de+la+Cultura+Pabellon+de+Arteaga+Aguascalientes",
    hasWater: true,
    waterType: "Bebedero de agua potable",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Patio con vegetación, bebedero y sombra constante."
  },
  // Municipio: Aguascalientes (Nuevos espacios de la lista de Google Maps)
  {
    id: 21,
    name: "Biblioteca Pública Enrique Fernández Ledesma",
    address: "Calle Galeana Norte 202, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 910 2010",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8807271,
    lng: -102.2977607,
    mapQuery: "Biblioteca+Enrique+Fernandez+Ledesma+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua fría para lectores",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Salas de lectura climatizadas frente al Jardín de San Marcos."
  },
  {
    id: 22,
    name: "Biblioteca Jaime Torres Bodet",
    address: "Andador Benito Juárez No. 122, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 910 2010",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8823929,
    lng: -102.2960414,
    mapQuery: "Biblioteca+Jaime+Torres+Bodet+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua purificada en entrada",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Ambiente climatizado silencioso con punto de hidratación en andador Juárez."
  },
  {
    id: 23,
    name: "Biblioteca Pública Central Centenario-Bicentenario",
    address: "Av. Manuel Gómez Morín S/N, Col. Ferronales, 20180 Aguascalientes, Ags.",
    phone: "449 910 2645",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.888821,
    lng: -102.279617,
    mapQuery: "Biblioteca+Centenario+Bicentenario+Aguascalientes",
    hasWater: true,
    waterType: "Estaciones de hidratación con filtro de carbono",
    hasAC: true,
    shadeLevel: "Alta",
    climateComfort: "Moderna biblioteca automatizada con climatización integral y agua purificada."
  },
  {
    id: 24,
    name: "Sala Alternativa",
    address: "Edén 205, Aguascalientes, Ags.",
    phone: "449 960 2333",
    type: "Teatro",
    municipio: "Aguascalientes",
    lat: 21.8983049,
    lng: -102.3178328,
    mapQuery: "Sala+Alternativa+Cine+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua en ambigú",
    hasAC: true,
    shadeLevel: "Media",
    climateComfort: "Sala de proyección y teatro climatizada con confort térmico interior."
  },
  {
    id: 25,
    name: "Aquelarre, espacio cultural",
    address: "Calle Francisco G. Hornedo No. 336, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "449 190 0616",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8800881,
    lng: -102.2924548,
    mapQuery: "Aquelarre+espacio+cultural+Aguascalientes",
    hasWater: true,
    waterType: "Garrafón de agua filtrada de libre acceso",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Foro independiente con patio sombreado e hidratación gratuita."
  },
  {
    id: 26,
    name: "Cafecheetoh",
    address: "Talamantes 213, Barrio de San Marcos, 20070 Aguascalientes, Ags.",
    phone: "No disponible",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8804204,
    lng: -102.305894,
    mapQuery: "Cafecheetoh+Café+Cultural+Aguascalientes",
    hasWater: true,
    waterType: "Vaso de agua potable purificada de cortesía",
    hasAC: false,
    shadeLevel: "Media",
    climateComfort: "Café cultural arbolado en San Marcos con agua de cortesía para clientes y transeúntes."
  },
  {
    id: 27,
    name: "Hogar Espejo",
    address: "Gral. Francisco Villa 245, Zona Centro, 20000 Aguascalientes, Ags.",
    phone: "No disponible",
    type: "Centro Cultural",
    municipio: "Aguascalientes",
    lat: 21.8898675,
    lng: -102.2867643,
    mapQuery: "Hogar+Espejo+Centro+Cultural+Aguascalientes",
    hasWater: true,
    waterType: "Dispensador de agua purificada ambiental",
    hasAC: false,
    shadeLevel: "Alta",
    climateComfort: "Jardín sombreado comunitario con libre acceso a agua fresca."
  }
];

// Atributos de confort e hidratación: respeta los datos explícitos y solo
// completa lo que falta (los registros remotos llegan incompletos a veces).
function normalizeSpaceComfort(space) {
  if (!space) return space;
  if (space.hasWater === undefined) {
    space.hasWater = true;
  }
  if (!space.waterType) {
    space.waterType = space.hasWater
      ? "Bebedero o dispensador de agua potable"
      : "Sin punto público de agua";
  }
  if (space.hasAC === undefined) {
    space.hasAC = ["Teatro", "Galería de Arte", "Centro Cultural"].includes(space.type);
  }
  if (!space.shadeLevel) {
    space.shadeLevel = (space.type === "Casa de Cultura" || space.type === "Centro Comunitario")
      ? "Alta"
      : "Media";
  }
  if (!space.climateComfort) {
    space.climateComfort = space.hasWater
      ? (space.hasAC
        ? "Punto de agua potable gratis y aire acondicionado."
        : "Acceso libre a agua potable y sombra natural.")
      : "Espacio techado adecuado para resguardo contra clima severo.";
  }
  return space;
}

spacesData.forEach(normalizeSpaceComfort);

// ==========================================================================
// 5. COMPOSICIÓN DEL CATÁLOGO ACTIVO
//    base incorporada + base remota + respaldo local + hallazgos de sesión.
//    Duplicado = mismo id o coordenadas a menos de 5x10^-4 grados (Req. 3.5).
// ==========================================================================

const TOLERANCIA_DUPLICADO = 5e-4;

function espacioValido(space) {
  return Boolean(space) && Boolean(space.name) &&
    !isNaN(parseFloat(space.lat)) && !isNaN(parseFloat(space.lng));
}

function mismoEspacio(a, b) {
  if (!a || !b) return false;
  if (a.id !== undefined && b.id !== undefined && a.id !== null && b.id !== null && a.id === b.id) {
    return true;
  }
  return Math.abs(parseFloat(a.lat) - parseFloat(b.lat)) < TOLERANCIA_DUPLICADO &&
    Math.abs(parseFloat(a.lng) - parseFloat(b.lng)) < TOLERANCIA_DUPLICADO;
}

let espaciosRemotos = [];   // registros de la base remota
let espaciosLocales = [];   // respaldo del navegador, pendiente de sincronizar
let espaciosSesion = [];    // hallazgos de la lista de Google Maps sin registro remoto
let activeSpacesList = [];

function cargarRespaldoLocal() {
  const guardado = leerAlmacen(CLAVE_RESPALDO);
  if (!guardado) return;
  try {
    const analizado = JSON.parse(guardado);
    if (Array.isArray(analizado)) {
      espaciosLocales = analizado.filter(espacioValido);
    }
  } catch (err) {
    espaciosLocales = [];
    avisar({
      titulo: "Respaldo local ilegible",
      cuerpo: "Los espacios guardados en este navegador no se pudieron leer. Se muestra el catálogo base.",
      tipo: "alerta",
      simbolo: "alerta",
      clave: "respaldo-ilegible"
    });
  }
}

function persistirRespaldoLocal() {
  if (espaciosLocales.length === 0) {
    return escribirAlmacen(CLAVE_RESPALDO, "[]");
  }
  return escribirAlmacen(CLAVE_RESPALDO, JSON.stringify(espaciosLocales));
}

// La cercanía de coordenadas solo indica duplicado entre orígenes distintos:
// dentro de un mismo origen hay recintos vecinos que comparten inmueble
// (Galería Benjamín Manzo e Instituto Cultural, Casa Terán y Centro Ángel),
// y ahí manda el identificador.
function recomponerCatalogo() {
  const resultado = [];

  const agregarOrigen = (lista) => {
    const corte = resultado.length;
    lista.forEach((space) => {
      if (!espacioValido(space)) return;

      const mismoOrigen = resultado.slice(corte);
      const yaEstaPorId = mismoOrigen.some(
        (otro) => otro.id !== undefined && otro.id !== null && otro.id === space.id
      );
      if (yaEstaPorId) return;

      const origenesPrevios = resultado.slice(0, corte);
      if (origenesPrevios.some((otro) => mismoEspacio(otro, space))) return;

      resultado.push(normalizeSpaceComfort(space));
    });
  };

  agregarOrigen(spacesData);
  agregarOrigen(espaciosRemotos);
  agregarOrigen(espaciosLocales);
  agregarOrigen(espaciosSesion);
  activeSpacesList = resultado;

  // El respaldo local deja de guardarse cuando el mismo espacio ya está
  // disponible en la base remota (Requisito 3.10).
  const restantes = espaciosLocales.filter(
    (local) => !espaciosRemotos.some((remoto) => mismoEspacio(remoto, local))
  );
  if (restantes.length !== espaciosLocales.length) {
    espaciosLocales = restantes;
    persistirRespaldoLocal();
  }
}

// Refresco completo de la interfaz tras cualquier cambio en el catálogo.
function refrescarCatalogo() {
  recomponerCatalogo();
  updateStatsTargets();
  filterData();
  if (map) switchMapLayer(activeMapLayer);
}

cargarRespaldoLocal();
recomponerCatalogo();

// ==========================================================================
// 6. BASE REMOTA (Supabase, cliente cargado desde el HTML)
// ==========================================================================

const SUPABASE_URL = "https://ebjejctuuhvmjmevabjh.supabase.co";
const SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImViamVqY3R1dWh2bWptZXZhYmpoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODMzMzg4NDksImV4cCI6MjA5ODkxNDg0OX0.MCVJau8KDz_xV1iwakVW4wpKD_MeMGan3aWY3cGE6g0";
const TABLA_ESPACIOS = "espacios_culturales";

let supabaseClient = null;
try {
  if (window.supabase && typeof window.supabase.createClient === "function") {
    supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_KEY);
  }
} catch (err) {
  supabaseClient = null;
}

function completarEspacio(space) {
  if (!space.mapQuery) {
    space.mapQuery = encodeURIComponent(
      space.name + " " + (space.municipio || "Aguascalientes") + " Aguascalientes"
    );
  }
  return normalizeSpaceComfort(space);
}

// Solo los registros marcados como remotos se actualizan en la base remota.
function prepararEspacioRemoto(space) {
  const listo = completarEspacio(space);
  listo.origenRemoto = true;
  delete listo.pendienteSync;
  return listo;
}

async function loadSpacesFromSupabase() {
  if (!supabaseClient) return;
  if (!enLinea()) {
    aplazarHastaConexion("supabase-catalogo", loadSpacesFromSupabase);
    return;
  }
  try {
    const consulta = supabaseClient.from(TABLA_ESPACIOS).select("*").order("id", { ascending: true });
    const { data, error } = await conLimite(consulta);
    if (error) throw error;

    if (Array.isArray(data) && data.length > 0) {
      espaciosRemotos = data.filter(espacioValido).map(prepararEspacioRemoto);
      refrescarCatalogo();
      if (map) setTimeout(() => map.invalidateSize(), 200);
    }
  } catch (err) {
    avisarFuenteCaida("la base de datos remota", "supabase-catalogo");
    if (!enLinea()) aplazarHastaConexion("supabase-catalogo", loadSpacesFromSupabase);
  }
}

function subscribeToSpacesRealtime() {
  if (!supabaseClient || typeof supabaseClient.channel !== "function") return;
  try {
    supabaseClient
      .channel("public:" + TABLA_ESPACIOS)
      .on(
        "postgres_changes",
        { event: "INSERT", schema: "public", table: TABLA_ESPACIOS },
        (payload) => {
          const nuevo = payload && payload.new;
          if (!espacioValido(nuevo)) return;
          if (espaciosRemotos.some((otro) => mismoEspacio(otro, nuevo))) return;
          espaciosRemotos.push(prepararEspacioRemoto(nuevo));
          refrescarCatalogo();
          avisar({
            titulo: "Espacio sincronizado",
            cuerpo: nuevo.name + " se añadió al directorio y al mapa.",
            tipo: "exito",
            simbolo: "sincronizar"
          });
        }
      )
      .subscribe();
  } catch (err) {
    avisar({
      titulo: "Sincronización en vivo no disponible",
      cuerpo: "Los espacios nuevos aparecerán al recargar o al sincronizar manualmente.",
      tipo: "alerta",
      simbolo: "alerta",
      clave: "realtime"
    });
  }
}

// Reintenta el alta remota de los espacios que solo viven en este navegador.
async function sincronizarPendientes() {
  if (!supabaseClient || !enLinea()) return;
  const pendientes = espaciosLocales.filter((space) => space.pendienteSync);
  if (pendientes.length === 0) return;

  let subidos = 0;
  for (const space of pendientes) {
    const carga = cargaRemota(space);
    try {
      const consulta = supabaseClient.from(TABLA_ESPACIOS).insert([carga]).select();
      const { data, error } = await conLimite(consulta);
      if (error) throw error;
      if (data && data[0]) {
        espaciosRemotos.push(prepararEspacioRemoto(data[0]));
        espaciosLocales = espaciosLocales.filter((otro) => otro !== space);
        subidos += 1;
      }
    } catch (err) {
      break;
    }
  }
  if (subidos > 0) {
    persistirRespaldoLocal();
    refrescarCatalogo();
    avisar({
      titulo: "Respaldo local sincronizado",
      cuerpo: subidos + (subidos === 1 ? " espacio pendiente" : " espacios pendientes") + " ya están en la base remota.",
      tipo: "exito",
      simbolo: "sincronizar"
    });
  }
}

// Campos que acepta la tabla remota (sin banderas internas de la interfaz).
function cargaRemota(space) {
  return {
    name: space.name,
    address: space.address,
    phone: space.phone,
    type: space.type,
    municipio: space.municipio,
    lat: space.lat,
    lng: space.lng,
    mapQuery: space.mapQuery,
    hasWater: space.hasWater,
    hasAC: space.hasAC,
    shadeLevel: space.shadeLevel,
    climateComfort: space.climateComfort
  };
}

window.addEventListener("offline", () => {
  avisar({
    titulo: "Sin conexión",
    cuerpo: "El buscador y los filtros siguen operando con el catálogo disponible. Los reintentos remotos esperan a que vuelva la red.",
    tipo: "alerta",
    simbolo: "alerta",
    clave: "sin-conexion",
    silenciarMs: 15000
  });
  cancelarSyncFondo();
});

window.addEventListener("online", () => {
  avisar({
    titulo: "Conexión restablecida",
    cuerpo: "Reanudamos las consultas remotas pendientes.",
    tipo: "exito",
    simbolo: "sincronizar",
    clave: "con-conexion",
    silenciarMs: 15000
  });
  ejecutarReintentos();
  sincronizarPendientes();
  programarSyncFondo();
});

// ==========================================================================
// 7. SINCRONIZACIÓN CON LA LISTA COMPARTIDA DE GOOGLE MAPS
// ==========================================================================

// Marcas pictográficas que aparecen en las notas del listado externo. Se
// comparan contra ese texto ajeno y nunca se muestran en la interfaz.
const MARCAS_NOTA = {
  agua: ["\u{1F6B0}", "\u{1F4A7}"],
  clima: ["\u2744"],
  sombra: ["\u{1F333}"],
  biblioteca: ["\u{1F4DA}"],
  teatro: ["\u{1F3AD}"],
  galeria: ["\u{1F5BC}"]
};

function notaIncluye(nota, palabras, marcas) {
  const encontrado = palabras.some((palabra) => nota.includes(palabra));
  if (encontrado) return true;
  return Array.isArray(marcas) ? marcas.some((marca) => nota.includes(marca)) : false;
}

function clasificarNota(nota) {
  const texto = String(nota || "").toLowerCase();

  const hasWater = notaIncluye(
    texto,
    ["agua", "bebedero", "dispensador", "hidratac", "potable"],
    MARCAS_NOTA.agua
  );

  const hasAC = notaIncluye(
    texto,
    ["a/c", "clima", "aire", "acondicionado"],
    MARCAS_NOTA.clima
  );

  let shadeLevel = "Media";
  if (notaIncluye(texto, ["sombra alta", "arbolado", "mucha sombra"], MARCAS_NOTA.sombra)) {
    shadeLevel = "Alta";
  } else if (notaIncluye(texto, ["sombra baja", "despejado", "soleado"], null)) {
    shadeLevel = "Baja";
  }

  let type = "Centro Cultural";
  if (notaIncluye(texto, ["biblioteca"], MARCAS_NOTA.biblioteca)) {
    type = "Biblioteca";
  } else if (notaIncluye(texto, ["teatro"], MARCAS_NOTA.teatro)) {
    type = "Teatro";
  } else if (notaIncluye(texto, ["galería", "galeria", "museo"], MARCAS_NOTA.galeria)) {
    type = "Galería de Arte";
  } else if (notaIncluye(texto, ["comunitario", "comuna"], null)) {
    type = "Centro Comunitario";
  } else if (notaIncluye(texto, ["casa de cultura", "casa cultura"], null)) {
    type = "Casa de Cultura";
  }

  return { hasWater, hasAC, shadeLevel, type };
}

const SYNC_INTERVALO = 5 * 60 * 1000;   // Requisito 5.9: 5 minutos como mínimo
let syncTemporizador = null;
let syncUltimoInicio = 0;
let syncEnCurso = false;

function cancelarSyncFondo() {
  if (syncTemporizador !== null) {
    clearTimeout(syncTemporizador);
    syncTemporizador = null;
  }
}

// Requisito 5.8: nada de peticiones mientras la pestaña está oculta.
function programarSyncFondo() {
  cancelarSyncFondo();
  if (document.visibilityState === "hidden") return;
  const espera = Math.max(0, SYNC_INTERVALO - (Date.now() - syncUltimoInicio));
  syncTemporizador = setTimeout(ejecutarSyncFondo, espera);
}

async function ejecutarSyncFondo() {
  syncTemporizador = null;
  if (document.visibilityState === "hidden") return;
  if (!enLinea()) {
    aplazarHastaConexion("sync-fondo", programarSyncFondo);
    return;
  }
  syncUltimoInicio = Date.now();
  await syncGoogleMapsList(false);
  programarSyncFondo();
}

document.addEventListener("visibilitychange", () => {
  if (document.visibilityState === "hidden") {
    cancelarSyncFondo();
  } else {
    programarSyncFondo();
  }
});

function rotularBotonesSync(sincronizando) {
  const principal = $("syncGmapsBtn");
  const mapa = $("syncGmapsBtnMap");
  if (principal) {
    principal.innerHTML = sincronizando
      ? icono("reloj") + "Sincronizando"
      : icono("sincronizar") + "Sincronizar Google Maps";
    principal.setAttribute("aria-busy", sincronizando ? "true" : "false");
  }
  if (mapa) {
    mapa.innerHTML = sincronizando
      ? icono("reloj") + "Sincronizando"
      : icono("sincronizar") + "Sincronizar en vivo";
    mapa.setAttribute("aria-busy", sincronizando ? "true" : "false");
  }
}

async function descargarListaGmaps() {
  const listaApi = "https://www.google.com/maps/preview/entitylist/getlist?authuser=0&hl=es&gl=mx&pb=!1m1!1sGzNGWQCSTaKUCoHh621-3g!2e2!3e2!4i500";
  const puentes = [
    "https://api.allorigins.win/raw?url=" + encodeURIComponent(listaApi),
    "https://corsproxy.io/?" + encodeURIComponent(listaApi)
  ];
  for (const puente of puentes) {
    try {
      const respuesta = await pedir(puente);
      const texto = await respuesta.text();
      if (texto && texto.length > 200) return texto;
    } catch (err) {
      // Se intenta el siguiente puente; el aviso visible llega al final.
    }
  }
  return "";
}

function lugaresDeLista(textoCrudo) {
  let limpio = String(textoCrudo).trim();
  if (limpio.startsWith(")]}'")) {
    limpio = limpio.substring(4).trim();
  }
  const analizado = JSON.parse(limpio);
  const info = Array.isArray(analizado) ? analizado[0] : null;
  return (info && info[8]) || [];
}

async function syncGoogleMapsList(userInitiated) {
  if (syncEnCurso) return;

  if (!enLinea()) {
    if (userInitiated) {
      avisar({
        titulo: "Sin conexión",
        cuerpo: "La sincronización con Google Maps se reanudará cuando vuelva la red.",
        tipo: "alerta",
        simbolo: "alerta",
        clave: "sync-sin-red"
      });
    }
    aplazarHastaConexion("sync-gmaps", () => syncGoogleMapsList(false));
    return;
  }

  syncEnCurso = true;
  syncUltimoInicio = Date.now();
  if (userInitiated) rotularBotonesSync(true);

  let agregados = 0;
  let actualizados = 0;
  let fallo = false;

  try {
    const texto = await descargarListaGmaps();
    if (!texto) throw new Error("Lista no disponible");

    const lugares = lugaresDeLista(texto);

    for (const lugar of lugares) {
      if (!lugar || lugar.length < 3) continue;

      const name = lugar[2] || "Espacio de la lista de Google Maps";
      const nota = lugar[3] || "";
      const coordenadas = lugar[1] && lugar[1].length > 5 ? lugar[1][5] : null;
      if (!coordenadas || coordenadas.length < 4) continue;

      const lat = parseFloat(coordenadas[2]);
      const lng = parseFloat(coordenadas[3]);
      if (isNaN(lat) || isNaN(lng)) continue;

      const rasgos = clasificarNota(nota);
      const climateComfort = nota || (rasgos.hasWater
        ? (rasgos.hasAC
          ? "Punto de agua potable gratis y aire acondicionado."
          : "Acceso libre a agua potable y sombra natural.")
        : "Espacio techado adecuado para resguardo contra clima severo.");

      const candidato = {
        name: name,
        address: "Aguascalientes",
        phone: "No disponible",
        type: rasgos.type,
        municipio: "Aguascalientes",
        lat: lat,
        lng: lng,
        mapQuery: encodeURIComponent(name + " Aguascalientes"),
        hasWater: rasgos.hasWater,
        hasAC: rasgos.hasAC,
        shadeLevel: rasgos.shadeLevel,
        climateComfort: climateComfort
      };

      const existente = activeSpacesList.find((space) => mismoEspacio(space, candidato));

      if (!existente) {
        let guardado = null;
        if (supabaseClient) {
          try {
            const consulta = supabaseClient.from(TABLA_ESPACIOS).insert([candidato]).select();
            const { data, error } = await conLimite(consulta);
            if (error) throw error;
            if (data && data[0]) guardado = data[0];
          } catch (err) {
            guardado = null;
          }
        }
        if (guardado) {
          espaciosRemotos.push(prepararEspacioRemoto(guardado));
        } else {
          candidato.id = Date.now() + Math.floor(Math.random() * 1000);
          espaciosSesion.push(completarEspacio(candidato));
        }
        agregados += 1;
      } else if (
        existente.hasWater !== rasgos.hasWater ||
        existente.hasAC !== rasgos.hasAC ||
        existente.shadeLevel !== rasgos.shadeLevel ||
        existente.type !== rasgos.type ||
        existente.climateComfort !== climateComfort
      ) {
        existente.hasWater = rasgos.hasWater;
        existente.hasAC = rasgos.hasAC;
        existente.shadeLevel = rasgos.shadeLevel;
        existente.type = rasgos.type;
        existente.climateComfort = climateComfort;

        if (supabaseClient && existente.origenRemoto && typeof existente.id === "number") {
          try {
            const consulta = supabaseClient
              .from(TABLA_ESPACIOS)
              .update({
                hasWater: rasgos.hasWater,
                hasAC: rasgos.hasAC,
                shadeLevel: rasgos.shadeLevel,
                type: rasgos.type,
                climateComfort: climateComfort
              })
              .eq("id", existente.id);
            await conLimite(consulta);
          } catch (err) {
            // El cambio queda aplicado en pantalla aunque el remoto falle.
          }
        }
        actualizados += 1;
      }
    }
  } catch (err) {
    fallo = true;
  } finally {
    syncEnCurso = false;
    if (userInitiated) rotularBotonesSync(false);
  }

  if (agregados > 0 || actualizados > 0) {
    refrescarCatalogo();
  }

  if (fallo) {
    if (userInitiated) {
      avisar({
        titulo: "Google Maps no respondió",
        cuerpo: "No pudimos leer la lista compartida. Se conserva el catálogo actual, que puede estar incompleto.",
        tipo: "error",
        simbolo: "alerta"
      });
    } else {
      avisarFuenteCaida("la lista compartida de Google Maps", "sync-gmaps");
    }
    return;
  }

  if (agregados > 0 || actualizados > 0) {
    const partes = [];
    if (agregados > 0) partes.push(agregados + (agregados === 1 ? " espacio nuevo" : " espacios nuevos"));
    if (actualizados > 0) partes.push(actualizados + (actualizados === 1 ? " ficha actualizada" : " fichas actualizadas"));
    avisar({
      titulo: "Sincronización completada",
      cuerpo: partes.join(" y ") + " desde la lista de Google Maps.",
      tipo: "exito",
      simbolo: "sincronizar"
    });
  } else if (userInitiated) {
    avisar({
      titulo: "Catálogo al día",
      cuerpo: activeSpacesList.length + " espacios coinciden con la lista de Google Maps.",
      tipo: "exito",
      simbolo: "sincronizar"
    });
  }
}

// ==========================================================================
// 8. CLIMA, GPS Y RECOMENDACIONES DE CONFORT
// ==========================================================================

let currentWeatherData = {
  temp: 26,
  feelsLike: 27,
  humidity: 40,
  uv: "7.2",
  wind: 12,
  muniName: "Aguascalientes"
};

let userLocation = null;
let activeHeatMode = "island";
let activeWaterSubfilter = "all";
let waterRoutePolyline = null;

// Coordenadas centrales de los 11 municipios de Aguascalientes
const MUNICIPIOS_DATA = {
  "Aguascalientes": { lat: 21.8823, lng: -102.2978, weather: null },
  "Calvillo": { lat: 21.8464, lng: -102.7187, weather: null },
  "Jesús María": { lat: 21.9611, lng: -102.3433, weather: null },
  "Rincón de Romos": { lat: 22.2289, lng: -102.3231, weather: null },
  "San José de Gracia": { lat: 22.1528, lng: -102.4158, weather: null },
  "Pabellón de Arteaga": { lat: 22.1414, lng: -102.2764, weather: null },
  "San Francisco de los Romo": { lat: 22.0733, lng: -102.2703, weather: null },
  "Asientos": { lat: 22.2386, lng: -102.0889, weather: null },
  "Cosío": { lat: 22.3650, lng: -102.3000, weather: null },
  "El Llano": { lat: 21.9181, lng: -101.9647, weather: null },
  "Tepezalá": { lat: 22.2217, lng: -102.1706, weather: null }
};

const CENTRO_ESTADO = { lat: 21.882, lng: -102.298 };

// Fórmula de Haversine para distancia en metros
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371e3;
  const f1 = lat1 * Math.PI / 180;
  const f2 = lat2 * Math.PI / 180;
  const df = (lat2 - lat1) * Math.PI / 180;
  const dl = (lon2 - lon1) * Math.PI / 180;

  const a = Math.sin(df / 2) * Math.sin(df / 2) +
    Math.cos(f1) * Math.cos(f2) * Math.sin(dl / 2) * Math.sin(dl / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

function formatDistance(metros) {
  if (metros < 1000) return Math.round(metros) + " m";
  return (metros / 1000).toFixed(1) + " km";
}

function referenciaUsuario() {
  return userLocation ? userLocation : CENTRO_ESTADO;
}

// Clasificación del índice UV (Requisito 3.3)
function getUVCategory(uvValue) {
  const val = parseFloat(uvValue) || 0;
  if (val < 3.0) return { tag: "Bajo", class: "uv-low" };
  if (val < 6.0) return { tag: "Moderado", class: "uv-mod" };
  if (val < 8.0) return { tag: "Alto", class: "uv-high" };
  return { tag: "Extremo", class: "uv-extreme" };
}

async function fetchCurrentWeather(lat, lng, muniName) {
  const latitud = typeof lat === "number" ? lat : CENTRO_ESTADO.lat;
  const longitud = typeof lng === "number" ? lng : CENTRO_ESTADO.lng;
  const nombre = muniName || "Aguascalientes";

  if (!enLinea()) {
    currentWeatherData.muniName = nombre;
    updateWeatherUI();
    updateComfortRecommendation();
    aplazarHastaConexion("clima", () => fetchCurrentWeather(latitud, longitud, nombre));
    return;
  }

  try {
    const url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitud +
      "&longitude=" + longitud +
      "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,uv_index&timezone=auto";
    const respuesta = await pedir(url);
    const datos = await respuesta.json();

    if (datos && datos.current) {
      const c = datos.current;
      currentWeatherData = {
        temp: Math.round(c.temperature_2m),
        feelsLike: Math.round(c.apparent_temperature),
        humidity: Math.round(c.relative_humidity_2m),
        uv: c.uv_index !== undefined && c.uv_index !== null ? Number(c.uv_index).toFixed(1) : "6.5",
        wind: Math.round(c.wind_speed_10m),
        muniName: nombre
      };
    }
    updateWeatherUI();
    updateComfortRecommendation();
    await fetchMunicipiosWeather();
  } catch (err) {
    currentWeatherData.muniName = nombre;
    updateWeatherUI();
    updateComfortRecommendation();
    avisarFuenteCaida("el servicio de clima Open-Meteo", "clima");
    if (!enLinea()) {
      aplazarHastaConexion("clima", () => fetchCurrentWeather(latitud, longitud, nombre));
    }
    await fetchMunicipiosWeather();
  }
}

async function fetchMunicipiosWeather() {
  const claves = Object.keys(MUNICIPIOS_DATA);

  const estimarLocalmente = () => {
    claves.forEach((nombre) => {
      const diferencia = nombre === "Calvillo" ? 2
        : (nombre === "San José de Gracia" ? -2 : (nombre === "Cosío" ? -1 : 0));
      MUNICIPIOS_DATA[nombre].weather = {
        temp: currentWeatherData.temp + diferencia,
        feelsLike: currentWeatherData.feelsLike + diferencia,
        humidity: currentWeatherData.humidity,
        uv: currentWeatherData.uv,
        wind: currentWeatherData.wind
      };
    });
  };

  if (!enLinea()) {
    estimarLocalmente();
    return;
  }

  try {
    const lats = claves.map((k) => MUNICIPIOS_DATA[k].lat).join(",");
    const lngs = claves.map((k) => MUNICIPIOS_DATA[k].lng).join(",");
    const url = "https://api.open-meteo.com/v1/forecast?latitude=" + lats + "&longitude=" + lngs +
      "&current=temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,uv_index&timezone=auto";
    const respuesta = await pedir(url);
    const datos = await respuesta.json();
    const lista = Array.isArray(datos) ? datos : [datos];

    lista.forEach((item, indice) => {
      const nombre = claves[indice];
      if (item && item.current && nombre) {
        const c = item.current;
        MUNICIPIOS_DATA[nombre].weather = {
          temp: Math.round(c.temperature_2m),
          feelsLike: Math.round(c.apparent_temperature),
          humidity: Math.round(c.relative_humidity_2m),
          uv: c.uv_index !== undefined && c.uv_index !== null ? Number(c.uv_index).toFixed(1) : "6.0",
          wind: Math.round(c.wind_speed_10m)
        };
      }
    });
  } catch (err) {
    estimarLocalmente();
    avisarFuenteCaida("el microclima municipal de Open-Meteo", "clima-municipios");
  }

  if (activeMapLayer === "weather" && map) buildWeatherMapLayer();
}

function updateWeatherUI() {
  const tempEl = $("weatherTemp");
  const feelsEl = $("weatherFeels");
  const humEl = $("weatherHumidity");
  const humDescEl = $("weatherHumidityDesc");
  const uvEl = $("weatherUV");
  const uvBadgeEl = $("weatherUVBadge");
  const windEl = $("weatherWind");
  const airQualityEl = $("weatherAirQuality");

  if (tempEl) tempEl.textContent = currentWeatherData.temp + " °C";
  if (feelsEl) feelsEl.textContent = "Sensación: " + currentWeatherData.feelsLike + " °C";

  if (humEl) humEl.textContent = currentWeatherData.humidity + " %";
  if (humDescEl) {
    const hum = currentWeatherData.humidity;
    humDescEl.textContent = hum < 30 ? "Ambiente seco" : (hum > 65 ? "Ambiente húmedo" : "Confort agradable");
  }

  if (uvEl) uvEl.textContent = currentWeatherData.uv;
  if (uvBadgeEl) {
    const categoria = getUVCategory(currentWeatherData.uv);
    uvBadgeEl.innerHTML = '<span class="uv-tag ' + categoria.class + '">' + esc(categoria.tag) + "</span>";
  }

  if (windEl) windEl.textContent = currentWeatherData.wind + " km/h";
  if (airQualityEl) {
    airQualityEl.textContent = currentWeatherData.wind > 20 ? "Viento moderado" : "Aire limpio y fresco";
  }
}

function updateComfortRecommendation(preferredService) {
  const recBadge = $("climaRecBadge");
  const recTitle = $("climaRecTitle");
  const recDesc = $("climaRecDesc");
  if (!recTitle || !recDesc) return;

  const referencia = referenciaUsuario();
  const ordenados = activeSpacesList
    .map((space) => {
      normalizeSpaceComfort(space);
      return Object.assign({}, space, {
        distMeters: calculateDistance(referencia.lat, referencia.lng, space.lat, space.lng)
      });
    })
    .sort((a, b) => a.distMeters - b.distMeters);

  if (ordenados.length === 0) return;

  const temp = currentWeatherData.temp;
  let elegido = null;
  let titulo = "";
  let descripcion = "";

  if (preferredService === "water") {
    elegido = ordenados.find((space) => space.hasWater) || ordenados[0];
    titulo = icono("gota") + " Punto de agua gratis más cercano a " + formatDistance(elegido.distMeters);
    descripcion = "Te recomendamos acudir a <strong>" + esc(elegido.name) + "</strong> (" +
      esc(elegido.address) + "). " + esc(elegido.waterType);
  } else if (preferredService === "shade" || temp >= 27) {
    elegido = ordenados.find((space) => space.hasAC || space.shadeLevel === "Alta") || ordenados[0];
    titulo = temp >= 27
      ? icono("fuego") + " Clima caluroso (" + temp + " °C): refugio climatizado a " + formatDistance(elegido.distMeters)
      : icono("arbol") + " Espacio fresco con sombra a " + formatDistance(elegido.distMeters);
    descripcion = "Te sugerimos resguardarte en <strong>" + esc(elegido.name) + "</strong>. " +
      esc(elegido.climateComfort);
  } else if (preferredService === "warm" || temp <= 16) {
    elegido = ordenados.find((space) => space.hasAC || space.type === "Teatro" || space.type === "Casa de Cultura") || ordenados[0];
    titulo = icono("viento") + " Clima fresco (" + temp + " °C): espacio techado a " + formatDistance(elegido.distMeters);
    descripcion = "Te recomendamos ingresar a <strong>" + esc(elegido.name) +
      "</strong>. Instalaciones acogedoras y protegidas del viento exterior.";
  } else {
    elegido = ordenados[0];
    titulo = icono("sol") + " Clima templado (" + temp + " °C): espacio cultural a " + formatDistance(elegido.distMeters);
    descripcion = "Visita <strong>" + esc(elegido.name) + "</strong> (" + esc(elegido.municipio) +
      "). Condiciones ambientales óptimas para tu recorrido.";
  }

  if (recBadge) {
    recBadge.innerHTML = userLocation
      ? icono("ubicacion") + " Recomendación con GPS activo"
      : icono("sol") + " Alerta climática ciudadana";
  }
  recTitle.innerHTML = titulo;
  recDesc.innerHTML = descripcion;
}

// --- Geolocalización (Requisito 10.5) ---
const MOTIVOS_GPS = {
  1: {
    titulo: "Permiso de ubicación denegado",
    cuerpo: "Conservamos la estación municipal seleccionada y sus datos de clima. Puedes volver a intentarlo cuando quieras."
  },
  2: {
    titulo: "Ubicación no disponible",
    cuerpo: "Tu dispositivo no pudo determinar la posición. Seguimos con la estación municipal seleccionada."
  },
  3: {
    titulo: "Tiempo de espera excedido",
    cuerpo: "La ubicación tardó más de 10 segundos. Seguimos con la estación municipal seleccionada."
  }
};

function rotularBotonGps(estado) {
  const boton = $("gpsLocateBtn");
  if (!boton) return;
  if (estado === "buscando") {
    boton.innerHTML = icono("reloj") + "Obteniendo tu ubicación";
    boton.setAttribute("aria-busy", "true");
    return;
  }
  boton.setAttribute("aria-busy", "false");
  boton.innerHTML = estado === "activo"
    ? icono("ubicacion") + "GPS activo"
    : icono("ubicacion") + "Activar mi ubicación GPS";
}

function activateGPSLocation() {
  if (!navigator.geolocation || typeof navigator.geolocation.getCurrentPosition !== "function") {
    rotularBotonGps("inactivo");
    avisar({
      titulo: "Función no disponible",
      cuerpo: "Este navegador no expone la API de geolocalización. Conservamos la estación municipal seleccionada y sus datos.",
      tipo: "alerta",
      simbolo: "alerta",
      clave: "gps-no-disponible"
    });
    return;
  }

  rotularBotonGps("buscando");

  navigator.geolocation.getCurrentPosition(
    (posicion) => {
      userLocation = { lat: posicion.coords.latitude, lng: posicion.coords.longitude };
      rotularBotonGps("activo");
      fetchCurrentWeather(userLocation.lat, userLocation.lng, "Tu ubicación");
      marcarUbicacionUsuario();
      updateComfortRecommendation();
    },
    (error) => {
      rotularBotonGps("inactivo");
      const motivo = MOTIVOS_GPS[error && error.code] || {
        titulo: "No pudimos obtener tu ubicación",
        cuerpo: "Conservamos la estación municipal seleccionada y sus datos de clima."
      };
      // Sin reintento automático: el visitante decide si vuelve a pedirlo.
      avisar({
        titulo: motivo.titulo,
        cuerpo: motivo.cuerpo,
        tipo: "alerta",
        simbolo: "ubicacion",
        clave: "gps-" + (error && error.code ? error.code : "generico"),
        silenciarMs: 5000
      });
    },
    { enableHighAccuracy: true, timeout: TIEMPO_LIMITE, maximumAge: 0 }
  );
}

function marcarUbicacionUsuario() {
  if (!map || !userLocation || typeof L === "undefined") return;

  if (marcadorUsuario) {
    marcadorUsuario.setLatLng([userLocation.lat, userLocation.lng]);
  } else {
    const puntoHtml = '<span style="display:block;inline-size:var(--esp-3);block-size:var(--esp-3);' +
      "background-color:var(--acento-2);border:var(--linea-2) solid var(--sup-hundida);" +
      'border-radius:var(--radio-pil);box-shadow:var(--sombra-2);"></span>';
    const iconoUsuario = L.divIcon({
      className: "user-gps-marker",
      iconAnchor: [8, 8],
      popupAnchor: [0, -8],
      html: puntoHtml
    });
    marcadorUsuario = L.marker([userLocation.lat, userLocation.lng], {
      icon: iconoUsuario,
      title: "Tu ubicación actual"
    }).addTo(map);
    marcadorUsuario.bindPopup(() => {
      const caja = document.createElement("div");
      const titulo = nodo("h4");
      titulo.innerHTML = icono("ubicacion") + " Tu ubicación actual";
      caja.appendChild(titulo);
      caja.appendChild(nodo("p", null, "Las recomendaciones de confort se calculan desde este punto."));
      return caja;
    });
  }

  map.setView([userLocation.lat, userLocation.lng], 14);
  marcadorUsuario.openPopup();
}

// ==========================================================================
// 9. MAPA LEAFLET Y SUS CUATRO CAPAS
// ==========================================================================

let map = null;
let markersGroup = null;
let heatLayerGroup = null;
let waterLayerGroup = null;
let weatherLayerGroup = null;
let marcadorUsuario = null;
let activeMapLayer = "spaces";
const customMarkers = new Map();

// Tonos por tipo de recinto: mismos tokens que la leyenda de styles.css
const TONO_TIPO = {
  "Casa de Cultura": "--acento-2",
  "Teatro": "--sem-calor",
  "Centro Cultural": "--acento",
  "Galería de Arte": "--sem-arte",
  "Centro Comunitario": "--sem-fresco",
  "Biblioteca": "--sem-tibio"
};

function tokenTipo(type) {
  return TONO_TIPO[type] || "--texto-alto";
}

function initMap() {
  const contenedor = $("leafletMap");
  if (!contenedor) return;

  if (typeof L === "undefined") {
    avisar({
      titulo: "Mapa no disponible",
      cuerpo: "La biblioteca del mapa no se pudo cargar. El directorio, los filtros y el asistente climático siguen operando.",
      tipo: "error",
      simbolo: "alerta",
      clave: "leaflet"
    });
    return;
  }

  try {
    map = L.map("leafletMap", { scrollWheelZoom: false, zoomControl: true })
      .setView([22.0, -102.3], 9.5);

    L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      subdomains: "abcd",
      maxZoom: 20
    }).addTo(map);

    markersGroup = L.featureGroup().addTo(map);

    plotMapMarkers(activeSpacesList);

    setTimeout(() => {
      if (map) map.invalidateSize();
    }, 300);
  } catch (err) {
    map = null;
    avisar({
      titulo: "Mapa no disponible",
      cuerpo: "No pudimos iniciar el mapa interactivo. El directorio y el asistente climático siguen operando.",
      tipo: "error",
      simbolo: "alerta",
      clave: "leaflet-init"
    });
  }
}

// Marca el estado activo con clase y con aria-pressed (Requisito 6.13)
function marcarActivo(elementos, esActivo) {
  elementos.forEach((elemento) => {
    const activo = Boolean(esActivo(elemento));
    elemento.classList.toggle("active", activo);
    elemento.setAttribute("aria-pressed", activo ? "true" : "false");
  });
}

function initLayerSwitcherEvents() {
  document.querySelectorAll(".layer-chip").forEach((chip) => {
    chip.addEventListener("click", () => {
      switchMapLayer(chip.getAttribute("data-layer"));
    });
  });
}

function switchMapLayer(layerName) {
  activeMapLayer = layerName;

  marcarActivo(
    Array.from(document.querySelectorAll(".layer-chip")),
    (chip) => chip.getAttribute("data-layer") === layerName
  );

  // Los paneles flotantes se muestran con style.display sobre [hidden]
  const paneles = {
    heat: $("heatOverlayPanel"),
    water: $("waterOverlayPanel"),
    weather: $("weatherOverlayPanel")
  };
  Object.keys(paneles).forEach((clave) => {
    if (paneles[clave]) paneles[clave].style.display = layerName === clave ? "block" : "none";
  });

  if (!map) return;

  [markersGroup, heatLayerGroup, waterLayerGroup, weatherLayerGroup].forEach((capa) => {
    if (capa && map.hasLayer(capa)) map.removeLayer(capa);
  });

  if (layerName === "spaces") {
    if (markersGroup) markersGroup.addTo(map);
  } else if (layerName === "heat") {
    buildHeatmapLayer();
    if (heatLayerGroup) heatLayerGroup.addTo(map);
  } else if (layerName === "water") {
    buildWaterMapLayer();
    if (waterLayerGroup) waterLayerGroup.addTo(map);
  } else if (layerName === "weather") {
    buildWeatherMapLayer();
    if (weatherLayerGroup) weatherLayerGroup.addTo(map);
  }
}

// --- Capa de recintos ---
function plotMapMarkers(spaces) {
  if (!map || !markersGroup) return;

  markersGroup.clearLayers();
  customMarkers.clear();

  const validos = spaces.filter(espacioValido);

  validos.forEach((space) => {
    const pinHtml = '<span style="display:block;inline-size:var(--esp-3);block-size:var(--esp-3);' +
      "background-color:var(" + tokenTipo(space.type) + ");" +
      "border:var(--linea-1) solid var(--sup-hundida);" +
      "border-radius:var(--radio-pil) var(--radio-pil) 0;transform:rotate(-45deg);" +
      'box-shadow:var(--sombra-2);"></span>';

    const iconoPin = L.divIcon({
      className: "custom-div-icon",
      iconAnchor: [0, 14],
      popupAnchor: [7, -14],
      html: pinHtml
    });

    const marcador = L.marker([space.lat, space.lng], { icon: iconoPin, title: space.name });
    marcador.bindPopup(() => popupEspacio(space));
    markersGroup.addLayer(marcador);
    customMarkers.set(space.id, marcador);
  });

  if (validos.length > 0) {
    map.fitBounds(markersGroup.getBounds(), { padding: [30, 30] });
  }
}

function filaConfort(space) {
  const fila = nodo("span", "card-comfort-row");
  if (space.hasWater) fila.appendChild(pastilla("badge-comfort badge-water", "agua", "Agua gratis"));
  if (space.hasAC) fila.appendChild(pastilla("badge-comfort badge-ac", "aire", "Climatización"));
  if (space.shadeLevel === "Alta") fila.appendChild(pastilla("badge-comfort badge-shade", "arbol", "Sombra alta"));
  if (space.pendienteSync) fila.appendChild(pastilla("space-badge", "sincronizar", "Sincronización pendiente", true));
  return fila;
}

function botonFicha(space, clase, texto) {
  const boton = document.createElement("button");
  boton.type = "button";
  boton.className = clase;
  boton.innerHTML = esc(texto) + icono("flecha-derecha");
  boton.addEventListener("click", () => openSpaceDrawer(space));
  return boton;
}

function popupEspacio(space) {
  const caja = document.createElement("div");
  caja.appendChild(nodo("h4", null, space.name));
  caja.appendChild(nodo("p", null, space.address));
  const confort = filaConfort(space);
  if (confort.childElementCount > 0) caja.appendChild(confort);
  caja.appendChild(botonFicha(space, "leaflet-popup-link", "Ver ficha completa"));
  return caja;
}

// --- Capa de mapa de calor, con sus modos isla de calor y confort ---
function clasificarTermica(spotTemp) {
  if (spotTemp >= 30) {
    return { etiqueta: "Isla de calor intenso", clase: "uv-tag uv-extreme", simbolo: "fuego", token: "--sem-calor" };
  }
  if (spotTemp >= 26) {
    return { etiqueta: "Temperatura elevada", clase: "uv-tag uv-high", simbolo: "sol", token: "--sem-tibio" };
  }
  if (spotTemp >= 21) {
    return { etiqueta: "Confort térmico óptimo", clase: "uv-tag uv-low", simbolo: "hoja", token: "--sem-fresco" };
  }
  return { etiqueta: "Zona fresca y de refugio", clase: "badge-comfort badge-water", simbolo: "gota", token: "--sem-agua" };
}

function actualizarInsigniaCalor() {
  const insignia = $("heatModeBadge");
  if (!insignia) return;
  insignia.innerHTML = activeHeatMode === "shade"
    ? icono("arbol") + "&#160;Cobertura de sombra"
    : icono("fuego") + "&#160;Isla de calor urbano";
}

function buildHeatmapLayer() {
  if (!map || typeof L === "undefined") return;
  if (heatLayerGroup && map.hasLayer(heatLayerGroup)) map.removeLayer(heatLayerGroup);

  heatLayerGroup = L.featureGroup();

  const modoSombra = activeHeatMode === "shade";
  actualizarInsigniaCalor();

  activeSpacesList.forEach((space) => {
    normalizeSpaceComfort(space);

    const baseMuni = MUNICIPIOS_DATA[space.municipio] && MUNICIPIOS_DATA[space.municipio].weather;
    const tempBase = (baseMuni && baseMuni.temp) || currentWeatherData.temp || 26;
    const ajusteSombra = space.shadeLevel === "Alta" ? -3.5 : (space.shadeLevel === "Media" ? -1.0 : 2.5);
    const ajusteClima = space.hasAC ? -1.5 : 0;
    const spotTemp = Math.round((tempBase + ajusteSombra + ajusteClima) * 10) / 10;

    let estado = clasificarTermica(spotTemp);
    if (modoSombra) {
      estado = space.shadeLevel === "Alta"
        ? { etiqueta: "Alta sombra y frescor", clase: "uv-tag uv-low", simbolo: "arbol", token: "--sem-fresco" }
        : { etiqueta: "Sombra parcial", clase: "uv-tag uv-mod", simbolo: "sol", token: "--acento-2" };
    }

    const trazo = colorToken(estado.token);

    const circulo = L.circle([space.lat, space.lng], {
      color: trazo,
      fillColor: trazo,
      fillOpacity: 0.35,
      weight: 2,
      radius: modoSombra ? (space.shadeLevel === "Alta" ? 450 : 250) : 380
    });

    circulo.bindPopup(() => {
      const caja = document.createElement("div");
      caja.appendChild(pastilla(estado.clase, estado.simbolo, estado.etiqueta, true));
      caja.appendChild(nodo("h4", null, space.name));
      caja.appendChild(nodo("p", null, space.address));

      const datos = nodo("span", "card-comfort-row");
      datos.appendChild(pastilla("badge-comfort badge-shade", "termometro", "En sitio: " + spotTemp + " °C"));
      datos.appendChild(pastilla("badge-comfort badge-shade", "arbol", "Sombra " + space.shadeLevel));
      datos.appendChild(pastilla("badge-comfort badge-ac", "aire", space.hasAC ? "Climatizado" : "Ventilación natural"));
      datos.appendChild(pastilla("badge-comfort badge-water", "agua", space.hasWater ? "Agua gratis" : "Sin agua pública"));
      caja.appendChild(datos);

      caja.appendChild(botonFicha(space, "leaflet-popup-link", "Ver ficha completa"));
      return caja;
    });

    heatLayerGroup.addLayer(circulo);
  });
}

// --- Capa de red de agua potable ---
function tonoAgua(waterType) {
  const texto = String(waterType || "").toLowerCase();
  if (texto.includes("bebedero") || texto.includes("toma")) return "--sem-agua";
  if (texto.includes("fría") || texto.includes("filtrad")) return "--acento";
  if (texto.includes("garraf")) return "--sem-fresco";
  return "--sem-agua";
}

function buildWaterMapLayer() {
  if (!map || typeof L === "undefined") return;
  if (waterLayerGroup && map.hasLayer(waterLayerGroup)) map.removeLayer(waterLayerGroup);

  waterLayerGroup = L.featureGroup();

  let puntos = activeSpacesList.map(normalizeSpaceComfort).filter((space) => space.hasWater);

  if (activeWaterSubfilter === "bebedero") {
    puntos = puntos.filter((space) => {
      const texto = space.waterType.toLowerCase();
      return texto.includes("bebedero") || texto.includes("toma");
    });
  } else if (activeWaterSubfilter === "fria") {
    puntos = puntos.filter((space) => {
      const texto = space.waterType.toLowerCase();
      return texto.includes("fría") || texto.includes("filtrad");
    });
  } else if (activeWaterSubfilter === "garrafon") {
    puntos = puntos.filter((space) => {
      const texto = space.waterType.toLowerCase();
      return texto.includes("garraf") || texto.includes("dispensador");
    });
  }

  const contador = $("waterStatsCount");
  if (contador) contador.textContent = puntos.length + (puntos.length === 1 ? " punto" : " puntos");

  puntos.forEach((space) => {
    const tono = tonoAgua(space.waterType);
    const gotaHtml = '<span class="water-pulse-icon" style="display:flex;align-items:center;' +
      "justify-content:center;inline-size:var(--toque-compacto);block-size:var(--toque-compacto);" +
      "color:var(--texto-inverso);background-color:var(" + tono + ");" +
      "border:var(--linea-2) solid var(--sup-hundida);border-radius:var(--radio-pil);" +
      'box-shadow:var(--sombra-2);">' + icono("agua") + "</span>";

    const iconoAgua = L.divIcon({
      className: "water-div-icon",
      iconAnchor: [18, 18],
      popupAnchor: [0, -18],
      html: gotaHtml
    });

    const marcador = L.marker([space.lat, space.lng], { icon: iconoAgua, title: space.name });

    marcador.bindPopup(() => {
      const referencia = referenciaUsuario();
      const distancia = calculateDistance(referencia.lat, referencia.lng, space.lat, space.lng);

      const caja = document.createElement("div");
      caja.appendChild(pastilla("space-badge", "agua", "Hidratación gratuita", true));
      caja.appendChild(nodo("h4", null, space.name));
      caja.appendChild(nodo("p", null, space.address));

      const detalle = nodo("span", "card-comfort-row");
      detalle.appendChild(pastilla("badge-comfort badge-water", "gota", space.waterType));
      detalle.appendChild(pastilla("badge-comfort badge-shade", "brujula", "A " + formatDistance(distancia)));
      caja.appendChild(detalle);

      const acciones = nodo("span", "mapa-actions");
      const ruta = document.createElement("button");
      ruta.type = "button";
      ruta.className = "btn btn-secondary btn-sm";
      ruta.innerHTML = icono("brujula") + "Trazar ruta";
      ruta.addEventListener("click", () => trazarRutaAgua(space));
      acciones.appendChild(ruta);
      acciones.appendChild(botonFicha(space, "btn btn-glow btn-sm", "Ficha"));
      caja.appendChild(acciones);

      return caja;
    });

    waterLayerGroup.addLayer(marcador);
  });
}

function trazarRutaAgua(space) {
  if (!space || !map || typeof L === "undefined") return;
  const referencia = referenciaUsuario();

  if (waterRoutePolyline && map.hasLayer(waterRoutePolyline)) {
    map.removeLayer(waterRoutePolyline);
  }

  const trazo = [[referencia.lat, referencia.lng], [space.lat, space.lng]];

  waterRoutePolyline = L.polyline(trazo, {
    color: colorToken("--acento-2"),
    weight: 4,
    opacity: 0.9,
    dashArray: "8, 12"
  }).addTo(map);

  map.fitBounds(L.latLngBounds(trazo), { padding: [60, 60] });

  if (!userLocation) {
    avisar({
      titulo: "Ruta desde el centro del estado",
      cuerpo: "Activa tu ubicación GPS para trazar la ruta desde donde estás.",
      tipo: "info",
      simbolo: "info",
      clave: "ruta-sin-gps"
    });
  }
}

function findNearestWaterPoints() {
  const referencia = referenciaUsuario();
  const ordenados = activeSpacesList
    .filter((space) => space.hasWater)
    .map((space) => Object.assign({}, space, {
      dist: calculateDistance(referencia.lat, referencia.lng, space.lat, space.lng)
    }))
    .sort((a, b) => a.dist - b.dist);

  if (ordenados.length === 0) {
    avisar({
      titulo: "Sin puntos de agua",
      cuerpo: "El catálogo actual no tiene puntos de hidratación registrados.",
      tipo: "alerta",
      simbolo: "alerta"
    });
    return;
  }

  switchMapLayer("water");
  const cercano = activeSpacesList.find((space) => mismoEspacio(space, ordenados[0]));
  trazarRutaAgua(cercano || ordenados[0]);
}

// --- Capa de estaciones meteorológicas ---
function buildWeatherMapLayer() {
  if (!map || typeof L === "undefined") return;
  if (weatherLayerGroup && map.hasLayer(weatherLayerGroup)) map.removeLayer(weatherLayerGroup);

  weatherLayerGroup = L.featureGroup();

  Object.keys(MUNICIPIOS_DATA).forEach((muniName) => {
    const info = MUNICIPIOS_DATA[muniName];
    const clima = info.weather || {
      temp: currentWeatherData.temp,
      feelsLike: currentWeatherData.feelsLike,
      humidity: currentWeatherData.humidity,
      uv: currentWeatherData.uv,
      wind: currentWeatherData.wind
    };

    const insigniaHtml = '<span style="display:inline-flex;align-items:center;gap:var(--esp-1);' +
      "padding:var(--esp-1) var(--esp-2);background-color:var(--sup-flotante);color:var(--texto-alto);" +
      "border:var(--linea-1) solid var(--borde-control);border-radius:var(--radio-pil);" +
      "font-family:var(--fuente-titulo);font-size:var(--txt-1);font-weight:var(--peso-fuerte);" +
      'white-space:nowrap;box-shadow:var(--sombra-2);">' + icono("nube-sol") +
      esc(muniName + ": " + clima.temp + " °C") + "</span>";

    const iconoClima = L.divIcon({
      className: "weather-div-icon",
      iconAnchor: [45, 14],
      popupAnchor: [0, -14],
      html: insigniaHtml
    });

    const marcador = L.marker([info.lat, info.lng], {
      icon: iconoClima,
      title: "Estación climática de " + muniName
    });

    marcador.bindPopup(() => {
      const recintos = activeSpacesList.filter((space) => space.municipio === muniName).length;

      const caja = document.createElement("div");
      const titulo = nodo("h4");
      titulo.innerHTML = icono("nube-sol") + " Estación climática de " + esc(muniName);
      caja.appendChild(titulo);

      const datos = nodo("span", "card-comfort-row");
      datos.appendChild(pastilla("badge-comfort badge-shade", "termometro", clima.temp + " °C"));
      datos.appendChild(pastilla("badge-comfort badge-ac", "fuego", "Sensación " + clima.feelsLike + " °C"));
      datos.appendChild(pastilla("badge-comfort badge-water", "gota", "Humedad " + clima.humidity + " %"));
      datos.appendChild(pastilla("badge-comfort badge-shade", "sol", "UV " + clima.uv));
      caja.appendChild(datos);

      caja.appendChild(nodo("p", null, recintos === 1
        ? "1 recinto registrado en este municipio."
        : recintos + " recintos registrados en este municipio."));

      if (recintos > 0) {
        const filtrar = document.createElement("button");
        filtrar.type = "button";
        filtrar.className = "btn btn-glow btn-sm";
        filtrar.innerHTML = icono("filtro") + "Filtrar recintos de " + esc(muniName);
        filtrar.addEventListener("click", () => filtrarPorMunicipio(muniName));
        caja.appendChild(filtrar);
      }

      return caja;
    });

    weatherLayerGroup.addLayer(marcador);
  });
}

// ==========================================================================
// 10. DIRECTORIO: BÚSQUEDA, FILTROS Y TARJETAS
// ==========================================================================

const searchInput = $("searchInput");
const clearSearchBtn = $("clearSearch");
const typeChips = Array.from(document.querySelectorAll("#typeChips .chip"));
const municipioChips = Array.from(document.querySelectorAll("#municipioChips .chip"));
const comfortChips = Array.from(document.querySelectorAll("#comfortChips .chip"));
const spacesGrid = $("spacesGrid");
const resultsCount = $("resultsCount");
const emptyState = $("emptyState");
const resetFiltersBtn = $("resetFiltersBtn");

let activeFilters = { search: "", type: "all", municipio: "all", comfort: "all" };

const CLASES_INSIGNIA = {
  "Casa de Cultura": "badge-casa",
  "Teatro": "badge-teatro",
  "Centro Cultural": "badge-centro",
  "Galería de Arte": "badge-galeria",
  "Centro Comunitario": "badge-comunitario",
  "Biblioteca": "badge-biblioteca"
};

function getBadgeClass(type) {
  return CLASES_INSIGNIA[type] || "badge-casa";
}

// Conteo visible de resultados sin mover el foco (Requisito 6.17)
function actualizarConteo(mostrados) {
  if (!resultsCount) return;
  const total = activeSpacesList.length;
  resultsCount.textContent = mostrados === 0
    ? "0 espacios encontrados con los filtros activos"
    : mostrados + " de " + total + (total === 1 ? " espacio disponible" : " espacios disponibles");
}

// Tarjeta alcanzable por teclado: rol de botón, Tabulador, Entrar y Barra
// espaciadora (Requisito 6.3). El encabezado h3 se conserva en el documento.
function crearTarjeta(space) {
  const tarjeta = nodo("div", "space-card");
  tarjeta.setAttribute("role", "button");
  tarjeta.tabIndex = 0;
  tarjeta.dataset.id = String(space.id);

  const distintivos =
    (space.hasWater ? '<span class="badge-comfort badge-water">' + icono("agua") + "Agua gratis</span>" : "") +
    (space.hasAC ? '<span class="badge-comfort badge-ac">' + icono("aire") + "Climatización</span>" : "") +
    (space.shadeLevel === "Alta" ? '<span class="badge-comfort badge-shade">' + icono("arbol") + "Sombra alta</span>" : "") +
    (space.pendienteSync
      ? '<span class="space-badge">' + icono("sincronizar") + "&#160;Sincronización pendiente</span>"
      : "");

  tarjeta.innerHTML =
    '<span class="space-card-top">' +
      '<span class="space-badge ' + getBadgeClass(space.type) + '">' + esc(space.type) + "</span>" +
      '<span class="space-municipio">' + esc(space.municipio) + "</span>" +
    "</span>" +
    "<h3>" + esc(space.name) + "</h3>" +
    '<p class="space-address">' + esc(space.address) + "</p>" +
    (distintivos ? '<span class="card-comfort-row">' + distintivos + "</span>" : "") +
    '<span class="space-card-footer">' +
      "<span>Tel: " + esc(space.phone) + "</span>" +
      '<span class="space-link">Detalles' + icono("flecha-derecha") + "</span>" +
    "</span>";

  const activar = () => abrirEspacioDesdeTarjeta(space);
  tarjeta.addEventListener("click", activar);
  tarjeta.addEventListener("keydown", (evento) => {
    if (evento.key === "Enter" || evento.key === " " || evento.key === "Spacebar") {
      evento.preventDefault();
      activar();
    }
  });

  return tarjeta;
}

function abrirEspacioDesdeTarjeta(space) {
  openSpaceDrawer(space);

  const marcador = customMarkers.get(space.id);
  if (map && marcador) {
    map.setView([space.lat, space.lng], 14);
    if (map.hasLayer(marcador)) marcador.openPopup();
    if (window.innerWidth < 1024) irASeccion($("mapa-section"));
  }
}

function renderDirectory(spaces) {
  if (!spacesGrid) return;
  spacesGrid.innerHTML = "";

  if (spaces.length === 0) {
    if (emptyState) emptyState.style.display = "block";
    spacesGrid.style.display = "none";
    actualizarConteo(0);
    return;
  }

  if (emptyState) emptyState.style.display = "none";
  spacesGrid.style.display = "grid";
  actualizarConteo(spaces.length);

  const lote = document.createDocumentFragment();
  spaces.forEach((space) => {
    normalizeSpaceComfort(space);
    lote.appendChild(crearTarjeta(space));
  });
  spacesGrid.appendChild(lote);
}

function filterData() {
  let filtrados = activeSpacesList;

  if (activeFilters.search) {
    const termino = activeFilters.search.toLowerCase().slice(0, 100);
    filtrados = filtrados.filter((space) =>
      String(space.name).toLowerCase().includes(termino) ||
      String(space.address).toLowerCase().includes(termino) ||
      String(space.municipio).toLowerCase().includes(termino)
    );
  }

  if (activeFilters.type !== "all") {
    filtrados = filtrados.filter((space) => space.type === activeFilters.type);
  }

  if (activeFilters.municipio !== "all") {
    filtrados = filtrados.filter((space) => space.municipio === activeFilters.municipio);
  }

  if (activeFilters.comfort === "water") {
    filtrados = filtrados.filter((space) => space.hasWater);
  } else if (activeFilters.comfort === "ac") {
    filtrados = filtrados.filter((space) => space.hasAC);
  } else if (activeFilters.comfort === "shade") {
    filtrados = filtrados.filter((space) => space.shadeLevel === "Alta");
  }

  renderDirectory(filtrados);
  plotMapMarkers(filtrados);
}

if (searchInput) {
  searchInput.addEventListener("input", (evento) => {
    activeFilters.search = evento.target.value;
    if (clearSearchBtn) clearSearchBtn.style.display = evento.target.value ? "block" : "none";
    filterData();
  });
}

if (clearSearchBtn) {
  clearSearchBtn.addEventListener("click", () => {
    if (searchInput) searchInput.value = "";
    activeFilters.search = "";
    clearSearchBtn.style.display = "none";
    filterData();
    if (searchInput) searchInput.focus();
  });
}

function enlazarChips(chips, atributo, campo) {
  chips.forEach((chip) => {
    chip.addEventListener("click", () => {
      const valor = chip.getAttribute(atributo);
      marcarActivo(chips, (otro) => otro === chip);
      activeFilters[campo] = valor;
      filterData();
    });
  });
}

enlazarChips(typeChips, "data-type", "type");
enlazarChips(municipioChips, "data-municipio", "municipio");
enlazarChips(comfortChips, "data-comfort", "comfort");

if (resetFiltersBtn) {
  resetFiltersBtn.addEventListener("click", () => {
    activeFilters = { search: "", type: "all", municipio: "all", comfort: "all" };
    if (searchInput) searchInput.value = "";
    if (clearSearchBtn) clearSearchBtn.style.display = "none";

    marcarActivo(typeChips, (chip) => chip.getAttribute("data-type") === "all");
    marcarActivo(municipioChips, (chip) => chip.getAttribute("data-municipio") === "all");
    marcarActivo(comfortChips, (chip) => chip.getAttribute("data-comfort") === "all");

    filterData();
  });
}

function filtrarPorMunicipio(muniName) {
  activeFilters.municipio = muniName;
  marcarActivo(municipioChips, (chip) => chip.getAttribute("data-municipio") === muniName);
  filterData();
  irASeccion($("espacios"));
}

// ==========================================================================
// 11. DIÁLOGOS: FOCO, CONFINAMIENTO Y ESCAPE (Requisitos 6.5 a 6.8)
// ==========================================================================

const SELECTOR_ENFOCABLE = [
  "a[href]",
  "button:not([disabled])",
  'input:not([disabled]):not([type="hidden"])',
  "select:not([disabled])",
  "textarea:not([disabled])",
  '[tabindex]:not([tabindex="-1"])'
].join(", ");

const pilaDialogos = [];

function elementosEnfocables(raiz) {
  return Array.from(raiz.querySelectorAll(SELECTOR_ENFOCABLE))
    .filter((elemento) => elemento.getClientRects().length > 0);
}

function enfocarEncabezado(encabezado) {
  if (!encabezado) return;
  if (!encabezado.hasAttribute("tabindex")) encabezado.setAttribute("tabindex", "-1");
  encabezado.focus({ preventScroll: true });
}

function encabezadoDeSeccion(elemento) {
  if (!elemento || typeof elemento.closest !== "function") return null;
  const seccion = elemento.closest("section");
  return seccion ? seccion.querySelector("h2") : null;
}

function devolverFoco(registro) {
  const origen = registro.origen;
  if (origen && document.contains(origen) && origen.getClientRects().length > 0) {
    origen.focus({ preventScroll: true });
    return;
  }
  enfocarEncabezado(registro.respaldo);
}

function abrirDialogo(config) {
  const abierto = pilaDialogos.find((registro) => registro.clave === config.clave);
  const activo = document.activeElement;
  const origen = activo && activo !== document.body ? activo : null;

  if (abierto) {
    // La ficha ya abierta cambia de espacio: el foco vuelve a su encabezado.
    if (origen && !abierto.dialogo.contains(origen)) {
      abierto.origen = origen;
      abierto.respaldo = encabezadoDeSeccion(origen);
    }
    enfocarEncabezado(abierto.encabezado);
    return;
  }

  pilaDialogos.push({
    clave: config.clave,
    dialogo: config.dialogo,
    encabezado: config.encabezado,
    cerrar: config.cerrar,
    origen: origen,
    respaldo: encabezadoDeSeccion(origen)
  });

  enfocarEncabezado(config.encabezado);
}

function cerrarDialogo(clave) {
  const indice = pilaDialogos.findIndex((registro) => registro.clave === clave);
  if (indice === -1) return;
  const registro = pilaDialogos.splice(indice, 1)[0];
  devolverFoco(registro);
}

// Confinamiento cíclico del foco en el diálogo más reciente
document.addEventListener("keydown", (evento) => {
  if (evento.key === "Escape") {
    if (pilaDialogos.length > 0) {
      evento.preventDefault();
      pilaDialogos[pilaDialogos.length - 1].cerrar();
      return;
    }
    if (navLinksWrapper && navLinksWrapper.classList.contains("active")) {
      evento.preventDefault();
      alternarMenu(false);
      if (navToggle) navToggle.focus();
    }
    return;
  }

  if (evento.key !== "Tab" || pilaDialogos.length === 0) return;

  const activo = pilaDialogos[pilaDialogos.length - 1];
  const enfocables = elementosEnfocables(activo.dialogo);
  const actual = document.activeElement;

  if (enfocables.length === 0) {
    evento.preventDefault();
    enfocarEncabezado(activo.encabezado);
    return;
  }

  const primero = enfocables[0];
  const ultimo = enfocables[enfocables.length - 1];

  if (!activo.dialogo.contains(actual)) {
    evento.preventDefault();
    (evento.shiftKey ? ultimo : primero).focus();
    return;
  }

  if (evento.shiftKey && (actual === primero || actual === activo.encabezado)) {
    evento.preventDefault();
    ultimo.focus();
  } else if (!evento.shiftKey && actual === ultimo) {
    evento.preventDefault();
    primero.focus();
  }
});

// --- Ficha de espacio ---
const drawerBackdrop = $("drawerBackdrop");
const spaceDrawer = $("spaceDrawer");
const drawerClose = $("drawerClose");
const drawerBadge = $("drawerBadge");
const drawerTitle = $("drawerTitle");
const drawerAddress = $("drawerAddress");
const drawerPhone = $("drawerPhone");
const drawerMunicipio = $("drawerMunicipio");
const drawerMapsLink = $("drawerMapsLink");

function openSpaceDrawer(space) {
  if (!drawerBackdrop || !spaceDrawer || !space) return;

  normalizeSpaceComfort(space);

  if (drawerBadge) {
    drawerBadge.textContent = space.type;
    drawerBadge.className = "space-badge " + getBadgeClass(space.type);
  }
  if (drawerTitle) drawerTitle.textContent = space.name;
  if (drawerAddress) drawerAddress.textContent = space.address;
  if (drawerPhone) drawerPhone.textContent = space.phone;
  if (drawerMunicipio) drawerMunicipio.textContent = space.municipio + " · Aguascalientes";
  if (drawerMapsLink) drawerMapsLink.href = "https://www.google.com/maps/search/" + space.mapQuery;

  const chipAgua = $("drawerWaterChip");
  const chipClima = $("drawerAcChip");
  const chipSombra = $("drawerShadeChip");
  const descripcion = $("drawerComfortDesc");

  if (chipAgua) chipAgua.style.display = space.hasWater ? "inline-flex" : "none";
  if (chipClima) chipClima.style.display = space.hasAC ? "inline-flex" : "none";
  // #drawerShadeChip es el span interno: el icono del chip queda intacto.
  if (chipSombra) chipSombra.textContent = "Sombra " + (space.shadeLevel || "Alta");
  if (descripcion) {
    descripcion.textContent = space.pendienteSync
      ? space.climateComfort + " Registro pendiente de sincronizar con la base remota."
      : space.climateComfort;
  }

  drawerBackdrop.classList.add("active");

  abrirDialogo({
    clave: "ficha",
    dialogo: spaceDrawer,
    encabezado: drawerTitle,
    cerrar: closeSpaceDrawer
  });
}

function closeSpaceDrawer() {
  if (drawerBackdrop) drawerBackdrop.classList.remove("active");
  cerrarDialogo("ficha");
}

if (drawerClose) drawerClose.addEventListener("click", closeSpaceDrawer);

if (drawerBackdrop) {
  drawerBackdrop.addEventListener("click", (evento) => {
    if (evento.target === drawerBackdrop) closeSpaceDrawer();
  });
}

// --- Formulario de alta ---
const openAddSpaceBtn = $("openAddSpaceBtn");
const addSpaceModalOverlay = $("addSpaceModalOverlay");
const addSpaceModal = $("addSpaceModal");
const addSpaceModalTitle = $("addSpaceModalTitle");
const closeAddSpaceModal = $("closeAddSpaceModal");
const addSpaceForm = $("addSpaceForm");

function abrirFormularioAlta() {
  if (!addSpaceModalOverlay || !addSpaceModal) return;
  addSpaceModalOverlay.classList.add("active");
  abrirDialogo({
    clave: "alta",
    dialogo: addSpaceModal,
    encabezado: addSpaceModalTitle,
    cerrar: cerrarFormularioAlta
  });
}

function cerrarFormularioAlta(conservarDatos) {
  if (!addSpaceModalOverlay) return;
  addSpaceModalOverlay.classList.remove("active");
  if (!conservarDatos && addSpaceForm) addSpaceForm.reset();
  cerrarDialogo("alta");
}

if (openAddSpaceBtn) openAddSpaceBtn.addEventListener("click", abrirFormularioAlta);
if (closeAddSpaceModal) closeAddSpaceModal.addEventListener("click", () => cerrarFormularioAlta(false));
if (addSpaceModalOverlay) {
  addSpaceModalOverlay.addEventListener("click", (evento) => {
    if (evento.target === addSpaceModalOverlay) cerrarFormularioAlta(false);
  });
}

// ==========================================================================
// 12. EXTRACTOR DE COORDENADAS (Requisitos 3.6 y 3.11)
// ==========================================================================

// Extensión del estado de Aguascalientes
const RANGO_AGS = { latMin: 21.6, latMax: 22.5, lngMin: -102.9, lngMax: -101.8 };

const PATRONES_COORDENADAS = [
  /@(-?\d+(?:\.\d+)?),(-?\d+(?:\.\d+)?)/,                               // arroba
  /[?&](?:q|ll|query|center|destination|daddr|sll)=(-?\d+(?:\.\d+)?),(-?\d+(?:\.\d+)?)/, // consulta
  /!3d(-?\d+(?:\.\d+)?)!4d(-?\d+(?:\.\d+)?)/,                           // datos del lugar
  /(-?\d+(?:\.\d+)?)\s*,\s*(-?\d+(?:\.\d+)?)/                           // texto simple
];

function dentroDeAguascalientes(lat, lng) {
  return lat >= RANGO_AGS.latMin && lat <= RANGO_AGS.latMax &&
    lng >= RANGO_AGS.lngMin && lng <= RANGO_AGS.lngMax;
}

function extractCoordsFromGmapsUrl(texto) {
  if (!texto || typeof texto !== "string") return null;

  for (const patron of PATRONES_COORDENADAS) {
    const coincidencia = texto.match(patron);
    if (!coincidencia) continue;
    const lat = parseFloat(coincidencia[1]);
    const lng = parseFloat(coincidencia[2]);
    if (isNaN(lat) || isNaN(lng)) continue;
    if (!dentroDeAguascalientes(lat, lng)) continue;
    return { lat: lat, lng: lng };
  }
  return null;
}

function avisarCoordenadasNoDetectadas() {
  avisar({
    titulo: "Coordenadas no detectadas",
    cuerpo: "El enlace no contiene un par de coordenadas dentro de Aguascalientes. Conservamos la latitud y la longitud que ya capturaste.",
    tipo: "alerta",
    simbolo: "alerta",
    clave: "coordenadas",
    silenciarMs: 8000
  });
}

const spaceGmapsUrlInput = $("spaceGmapsUrlInput");
if (spaceGmapsUrlInput) {
  let relojCoordenadas = null;
  let ultimoTextoAvisado = "";

  spaceGmapsUrlInput.addEventListener("input", (evento) => {
    const texto = evento.target.value.trim();
    if (relojCoordenadas) clearTimeout(relojCoordenadas);

    const encontrado = extractCoordsFromGmapsUrl(texto);
    if (encontrado) {
      const latEl = $("spaceLatInput");
      const lngEl = $("spaceLngInput");
      if (latEl) latEl.value = encontrado.lat;
      if (lngEl) lngEl.value = encontrado.lng;
      ultimoTextoAvisado = "";
      return;
    }

    // Sin par reconocible: no se toca lo capturado y se avisa una sola vez.
    if (texto.length >= 8 && texto !== ultimoTextoAvisado) {
      relojCoordenadas = setTimeout(() => {
        ultimoTextoAvisado = texto;
        avisarCoordenadasNoDetectadas();
      }, 700);
    }
  });
}

// ==========================================================================
// 13. ALTA DE ESPACIOS (base remota con respaldo local)
// ==========================================================================

const CENTROS_MUNICIPIO = {
  "Aguascalientes": [21.882, -102.298],
  "San José de Gracia": [22.153, -102.416],
  "San Francisco de los Romo": [22.073, -102.270],
  "Rincón de Romos": [22.229, -102.323],
  "Jesús María": [21.961, -102.343],
  "Calvillo": [21.846, -102.718],
  "Pabellón de Arteaga": [22.141, -102.276]
};

function valorCampo(id) {
  const campo = $(id);
  return campo ? campo.value.trim() : "";
}

if (addSpaceForm) {
  addSpaceForm.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const name = valorCampo("spaceNameInput");
    const address = valorCampo("spaceAddressInput");
    const type = valorCampo("spaceTypeInput");
    const municipio = valorCampo("spaceMunicipioInput");
    const phone = valorCampo("spacePhoneInput") || "No disponible";
    const consultaMapa = valorCampo("spaceMapQueryInput");
    const enlaceGmaps = valorCampo("spaceGmapsUrlInput");

    const desdeEnlace = extractCoordsFromGmapsUrl(enlaceGmaps);
    const latManual = parseFloat(valorCampo("spaceLatInput"));
    const lngManual = parseFloat(valorCampo("spaceLngInput"));
    const hayManuales = !isNaN(latManual) && !isNaN(lngManual);

    if (enlaceGmaps && !desdeEnlace && !hayManuales) {
      avisarCoordenadasNoDetectadas();
    }

    const centro = CENTROS_MUNICIPIO[municipio] || [21.9, -102.3];
    const desvioLat = (Math.random() - 0.5) * 0.008;
    const desvioLng = (Math.random() - 0.5) * 0.008;

    const lat = !isNaN(latManual) ? latManual : (desdeEnlace ? desdeEnlace.lat : centro[0] + desvioLat);
    const lng = !isNaN(lngManual) ? lngManual : (desdeEnlace ? desdeEnlace.lng : centro[1] + desvioLng);

    const mapQuery = consultaMapa ||
      (enlaceGmaps ? encodeURIComponent(enlaceGmaps) : encodeURIComponent(name + " " + municipio + " Aguascalientes"));

    const chipAgua = $("spaceWaterInput");
    const chipClima = $("spaceAcInput");
    const selectorSombra = $("spaceShadeInput");

    const hasWater = chipAgua ? chipAgua.checked : true;
    const hasAC = chipClima ? chipClima.checked : true;
    const shadeLevel = selectorSombra ? selectorSombra.value : "Alta";

    const nuevo = {
      name: name,
      address: address,
      phone: phone,
      type: type,
      municipio: municipio,
      lat: lat,
      lng: lng,
      mapQuery: mapQuery,
      hasWater: hasWater,
      hasAC: hasAC,
      shadeLevel: shadeLevel,
      climateComfort: hasWater
        ? "Punto de agua potable gratis y área de resguardo."
        : "Espacio techado con climatización de interiores."
    };

    let guardadoRemoto = null;
    let motivoRemoto = "";

    if (supabaseClient && enLinea()) {
      try {
        const consulta = supabaseClient.from(TABLA_ESPACIOS).insert([cargaRemota(nuevo)]).select();
        const { data, error } = await conLimite(consulta);
        if (error) throw error;
        if (data && data[0]) guardadoRemoto = data[0];
      } catch (err) {
        motivoRemoto = "La base remota rechazó el registro.";
      }
    } else if (!supabaseClient) {
      motivoRemoto = "La base remota no está disponible en esta sesión.";
    } else {
      motivoRemoto = "No hay conexión de red.";
    }

    if (guardadoRemoto) {
      espaciosRemotos.push(prepararEspacioRemoto(guardadoRemoto));
      refrescarCatalogo();
      avisar({
        titulo: "Espacio registrado",
        cuerpo: name + " se guardó en la base remota y ya aparece en el directorio y en el mapa.",
        tipo: "exito",
        simbolo: "mas"
      });
      cerrarFormularioAlta(false);
      return;
    }

    // Respaldo local con aviso de sincronización pendiente (Requisito 3.10)
    nuevo.id = Date.now();
    nuevo.pendienteSync = true;
    espaciosLocales.push(nuevo);
    const respaldado = persistirRespaldoLocal();
    refrescarCatalogo();

    if (respaldado) {
      avisar({
        titulo: "Sincronización pendiente",
        cuerpo: name + " se guardó en este navegador. " + motivoRemoto + " Lo enviaremos a la base remota cuando sea posible.",
        tipo: "alerta",
        simbolo: "sincronizar"
      });
      cerrarFormularioAlta(false);
      if (enLinea()) sincronizarPendientes();
      return;
    }

    // El almacenamiento local falló: los datos capturados quedan en pantalla
    // y el aviso indica el estado del registro remoto (Requisito 10.8).
    avisar({
      titulo: "El respaldo local no se guardó",
      cuerpo: "El almacenamiento de este navegador rechazó la escritura y el registro remoto no se completó. " +
        motivoRemoto + " Tus datos siguen en el formulario para reintentarlo.",
      tipo: "error",
      simbolo: "alerta"
    });
    aplazarHastaConexion("alta-" + nuevo.id, sincronizarPendientes);
  });
}

// ==========================================================================
// 14. NAVEGACIÓN: MENÚ, CINTA Y ENLACE ACTIVO
// ==========================================================================

const navToggle = $("navToggle");
const navLinksWrapper = document.querySelector(".nav-links-wrapper");
const navbar = $("navbar");
const navLinks = Array.from(document.querySelectorAll(".nav-links a"));

function alternarMenu(abrir) {
  if (!navToggle || !navLinksWrapper) return;
  navToggle.classList.toggle("active", abrir);
  navToggle.setAttribute("aria-expanded", abrir ? "true" : "false");
  navLinksWrapper.classList.toggle("active", abrir);
  document.body.style.overflow = abrir ? "hidden" : "";
}

if (navToggle && navLinksWrapper) {
  navToggle.addEventListener("click", () => {
    const abrir = !navLinksWrapper.classList.contains("active");
    alternarMenu(abrir);
    if (abrir) {
      const primero = navLinksWrapper.querySelector("a");
      if (primero) primero.focus();
    }
  });

  navLinksWrapper.querySelectorAll("a").forEach((enlace) => {
    enlace.addEventListener("click", () => alternarMenu(false));
  });

  document.addEventListener("click", (evento) => {
    if (!navLinksWrapper.classList.contains("active")) return;
    if (navLinksWrapper.contains(evento.target) || navToggle.contains(evento.target)) return;
    alternarMenu(false);
  });
}

// Enlace activo: sección con mayor superficie visible, empates por orden del
// documento, reflejo por debajo de 200 ms (Requisitos 4.6, 4.8 y 4.9).
const seccionesNav = navLinks
  .map((enlace) => {
    const destino = enlace.getAttribute("href") || "";
    const id = destino.startsWith("#") ? destino.slice(1) : "";
    const seccion = id ? $(id) : null;
    return seccion ? { enlace: enlace, seccion: seccion } : null;
  })
  .filter(Boolean);

let enlaceActivo = navLinks.find((enlace) => enlace.hasAttribute("aria-current")) || null;

function actualizarEnlaceActivo() {
  if (seccionesNav.length === 0) return;

  const alto = window.innerHeight || document.documentElement.clientHeight;
  let mejor = 0;
  let ganador = null;

  seccionesNav.forEach((par) => {
    const caja = par.seccion.getBoundingClientRect();
    const visible = Math.max(0, Math.min(caja.bottom, alto) - Math.max(caja.top, 0));
    if (visible > mejor + 0.5) {
      mejor = visible;
      ganador = par.enlace;
    }
  });

  if (!ganador || ganador === enlaceActivo) return;

  navLinks.forEach((enlace) => {
    if (enlace === ganador) {
      enlace.setAttribute("aria-current", "true");
    } else {
      enlace.removeAttribute("aria-current");
    }
  });
  enlaceActivo = ganador;
}

let cuadroPendiente = false;

function alDesplazar() {
  if (navbar) navbar.classList.toggle("scrolled", window.scrollY > 40);
  if (cuadroPendiente) return;
  cuadroPendiente = true;
  requestAnimationFrame(() => {
    cuadroPendiente = false;
    actualizarEnlaceActivo();
  });
}

window.addEventListener("scroll", alDesplazar, { passive: true });
window.addEventListener("resize", alDesplazar);

if (typeof IntersectionObserver === "function" && seccionesNav.length > 0) {
  const observador = new IntersectionObserver(() => actualizarEnlaceActivo(), {
    threshold: [0, 0.1, 0.25, 0.5, 0.75, 1]
  });
  seccionesNav.forEach((par) => observador.observe(par.seccion));
}

// ==========================================================================
// 15. CIFRAS DEL HERO
// ==========================================================================

function updateStatsTargets() {
  const totalEspacios = activeSpacesList.length;
  const totalMunicipios = new Set(activeSpacesList.map((space) => space.municipio)).size;
  const totalTipos = new Set(activeSpacesList.map((space) => space.type)).size;

  const cifras = [
    [document.querySelector(".stat-inline-box:nth-child(1) .stat-inline-num"), totalEspacios],
    [document.querySelector(".stat-inline-box:nth-child(2) .stat-inline-num"), totalMunicipios],
    [document.querySelector(".stat-inline-box:nth-child(3) .stat-inline-num"), totalTipos]
  ];

  cifras.forEach(([elemento, valor]) => {
    if (!elemento) return;
    elemento.setAttribute("data-target", String(valor));
    elemento.textContent = String(valor);
  });
}

function startStatsCounter() {
  const cifras = Array.from(document.querySelectorAll(".stat-inline-num"));
  if (cifras.length === 0) return;

  // Con movimiento reducido las cifras se muestran ya en su valor final.
  if (movimientoReducido() || typeof IntersectionObserver !== "function") {
    cifras.forEach((elemento) => {
      elemento.textContent = elemento.getAttribute("data-target") || elemento.textContent;
    });
    return;
  }

  const animar = (elemento) => {
    const objetivo = parseInt(elemento.getAttribute("data-target"), 10) || 0;
    const duracion = 1200;
    const inicio = performance.now();

    const paso = (ahora) => {
      const avance = Math.min((ahora - inicio) / duracion, 1);
      const suavizado = 1 - Math.pow(1 - avance, 4);
      elemento.textContent = String(Math.floor(suavizado * objetivo));
      if (avance < 1) {
        requestAnimationFrame(paso);
      } else {
        elemento.textContent = String(objetivo);
      }
    };

    requestAnimationFrame(paso);
  };

  const observador = new IntersectionObserver((entradas) => {
    entradas.forEach((entrada) => {
      if (entrada.isIntersecting) {
        animar(entrada.target);
        observador.unobserve(entrada.target);
      }
    });
  }, { threshold: 0.5 });

  cifras.forEach((elemento) => observador.observe(elemento));
}

// ==========================================================================
// 16. ARRANQUE
// ==========================================================================

function enlazarAsistenteClimatico() {
  const gpsBtn = $("gpsLocateBtn");
  if (gpsBtn) {
    rotularBotonGps("inactivo");
    gpsBtn.addEventListener("click", activateGPSLocation);
  }

  const btnAgua = $("btnFindWater");
  if (btnAgua) {
    btnAgua.addEventListener("click", () => {
      updateComfortRecommendation("water");
      switchMapLayer("water");
      irASeccion($("mapa-section"));
    });
  }

  const btnSombra = $("btnFindShade");
  if (btnSombra) {
    btnSombra.addEventListener("click", () => {
      updateComfortRecommendation("shade");
      switchMapLayer("heat");
      irASeccion($("mapa-section"));
    });
  }

  const btnTechado = $("btnFindWarm");
  if (btnTechado) {
    btnTechado.addEventListener("click", () => {
      updateComfortRecommendation("warm");
      switchMapLayer("weather");
      irASeccion($("mapa-section"));
    });
  }

  const chipsMunicipio = Array.from(document.querySelectorAll("#muniWeatherChips .muni-chip"));
  chipsMunicipio.forEach((chip) => {
    chip.addEventListener("click", () => {
      marcarActivo(chipsMunicipio, (otro) => otro === chip);
      const muni = chip.getAttribute("data-muni");
      const coordenadas = MUNICIPIOS_DATA[muni];
      if (!coordenadas) return;
      fetchCurrentWeather(coordenadas.lat, coordenadas.lng, muni);
      if (map) map.setView([coordenadas.lat, coordenadas.lng], 12);
    });
  });
}

function enlazarControlesMapa() {
  initLayerSwitcherEvents();

  const btnIsla = $("btnHeatModeIsland");
  const btnSombra = $("btnHeatModeShade");
  if (btnIsla && btnSombra) {
    const modos = [btnIsla, btnSombra];
    const cambiarModo = (modo, boton) => {
      activeHeatMode = modo;
      marcarActivo(modos, (otro) => otro === boton);
      actualizarInsigniaCalor();
      if (activeMapLayer === "heat") switchMapLayer("heat");
    };
    btnIsla.addEventListener("click", () => cambiarModo("island", btnIsla));
    btnSombra.addEventListener("click", () => cambiarModo("shade", btnSombra));
  }

  const subchips = Array.from(document.querySelectorAll("#waterSubfilters .water-chip"));
  subchips.forEach((chip) => {
    chip.addEventListener("click", () => {
      marcarActivo(subchips, (otro) => otro === chip);
      activeWaterSubfilter = chip.getAttribute("data-watersub");
      if (activeMapLayer === "water") switchMapLayer("water");
    });
  });

  const btnRuta = $("btnFindNearestWater");
  if (btnRuta) btnRuta.addEventListener("click", findNearestWaterPoints);

  const syncBtn = $("syncGmapsBtn");
  if (syncBtn) syncBtn.addEventListener("click", () => syncGoogleMapsList(true));

  const syncBtnMapa = $("syncGmapsBtnMap");
  if (syncBtnMapa) syncBtnMapa.addEventListener("click", () => syncGoogleMapsList(true));
}

function iniciar() {
  updateStatsTargets();
  initMap();
  filterData();
  startStatsCounter();
  actualizarEnlaceActivo();

  enlazarAsistenteClimatico();
  enlazarControlesMapa();
  rotularBotonesSync(false);

  fetchCurrentWeather();

  if (supabaseClient) {
    loadSpacesFromSupabase();
    subscribeToSpacesRealtime();
    sincronizarPendientes();
  }

  if (!enLinea()) {
    avisar({
      titulo: "Sin conexión",
      cuerpo: "Mostramos el catálogo local disponible. El buscador y los filtros siguen operando.",
      tipo: "alerta",
      simbolo: "alerta",
      clave: "sin-conexion"
    });
  }

  // Primera sincronización de fondo tras el intervalo mínimo de 5 minutos.
  syncUltimoInicio = Date.now();
  programarSyncFondo();
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", iniciar);
} else {
  iniciar();
}
