# Documento de Requisitos

## Introducción

Rediseño integral de la página principal de **VENDAVAL** (portal de espacios culturales y cultura climática de Aguascalientes, en colaboración con el IAJU). El objetivo es sustituir la apariencia de plantilla genérica por una identidad visual propia, con movimiento de calidad, rendimiento medible y accesibilidad verificable, **sin perder ninguna de las funciones que hoy ya operan**.

### Hallazgos del código actual que justifican el rediseño

Revisión de `index.html`, `styles.css` y `app.js`:

| Hallazgo | Efecto |
| --- | --- |
| Iconografía basada en emojis (🌡️ 💧 ☀️ 🔥 📍 …) en toda la interfaz | Es la señal más evidente de "hecho por IA"; el render cambia entre sistemas operativos |
| Combinación repetida de glassmorphism + degradado cian→azul + esferas de resplandor radial | Estética de plantilla, sin dirección de arte propia |
| CSS muerto (`.proyectos-section`, `.log-dashboard`, `.hero-stats-panel`, `.logo-spinner`) y estilos en línea dentro del HTML | Peso innecesario y mantenimiento frágil |
| Leaflet, leaflet.heat y Supabase se cargan desde tres CDNs distintos en la ruta crítica | Bloquea el primer render y depende de terceros |
| Auto-sincronización cada 30 s contra proxies CORS públicos | Consumo de batería y datos; expone las peticiones a terceros |
| Sin favicon, sin `og:image`, sin datos estructurados, `lang="es"` genérico | Compartir el enlace y el posicionamiento se ven pobres |
| Fallos de red resueltos solo con `console.error` | El visitante no recibe información del estado de los datos |

### Decisiones propuestas (a confirmar en la revisión)

1. **Stack**: mantener JavaScript estándar (sin framework de componentes) y añadir **Vite** como herramienta de compilación. Razón: toda la lógica actual (Leaflet, Supabase, Open-Meteo, GPS) es imperativa y funciona; migrarla a React/Vue añadiría riesgo sin beneficio visible para el visitante. Vite aporta empaquetado, versionado, minificación y desarrollo con recarga en caliente.
2. **Movimiento**: CSS moderno como base (animaciones ligadas al scroll, `@property`, transiciones de vista) y una biblioteca de movimiento ligera solo para las secuencias que CSS no cubre.
3. **Mapa**: Leaflet se conserva, empaquetado localmente y cargado de forma diferida.
4. **Fuera de alcance**: cambios de esquema en Supabase, nuevas fuentes de datos, tema claro, traducción a otros idiomas, aplicación móvil.

### Convención de redacción

Se usa EARS en español: `CUANDO` (evento), `MIENTRAS` (estado), `SI … ENTONCES` (situación no deseada), `DONDE` (opción configurable) y `DEBERÁ` como equivalente de *SHALL*.

## Glosario

- **Página_Principal**: documento de entrada del sitio (hoy `index.html`) con las secciones Hero, Asistente_Climático, Directorio, Mapa_Interactivo, Equipo y Pie_de_Página.
- **Sistema_de_Diseño**: conjunto de tokens (color, tipografía, espaciado, radios, sombras, duraciones, curvas) y componentes reutilizables que gobiernan la apariencia de la Página_Principal.
- **Motor_de_Movimiento**: subsistema responsable de las animaciones de entrada, los efectos ligados al desplazamiento y las microinteracciones.
- **Navegación_Principal**: barra superior fija con el logotipo y los enlaces a las secciones.
- **Hero**: primera sección de la Página_Principal, con la propuesta de valor y las acciones principales.
- **Asistente_Climático**: panel de temperatura, humedad, índice UV, viento, selector de estación municipal, activación de GPS y recomendaciones de confort.
- **Directorio**: sección de tarjetas filtrables de espacios culturales, con buscador y filtros de recinto, municipio y confort.
- **Mapa_Interactivo**: componente de mapa con las capas Recintos, Mapa de Calor, Mapa de Agua y Clima Actual.
- **Equipo**: sección que presenta al colectivo y a sus integrantes con nombre y rol.
- **Pie_de_Página**: última región de la Página_Principal, con marca, enlaces de navegación y créditos.
- **Ficha_de_Espacio**: panel deslizante (lateral en escritorio, inferior en móvil) con el detalle de un espacio.
- **Formulario_de_Alta**: diálogo modal "Agregar Espacio".
- **Catálogo_Activo**: unión del catálogo base incorporado, los espacios de la base remota y los espacios guardados en el almacenamiento local del navegador.
- **Extractor_de_Coordenadas**: función que obtiene latitud y longitud a partir de una URL de Google Maps.
- **Proyecto**: repositorio del sitio, con su código fuente, su configuración y sus dependencias declaradas.
- **Proceso_de_Compilación**: cadena de herramientas que transforma el código fuente en los archivos estáticos publicados.
- **Presupuesto_de_Rendimiento**: conjunto de límites numéricos declarados en el Requisito 5.
- **Dispositivo_de_Referencia**: perfil de medición equivalente a Moto G4 con CPU limitada 4× y red 4G simulada (10 Mbps de bajada, 40 ms de latencia).
- **Movimiento_Reducido**: estado en el que el sistema operativo del visitante declara `prefers-reduced-motion: reduce`.
- **Viewport_Móvil**: ancho de ventana de 320 px a 767 px.
- **Viewport_Tablet**: ancho de ventana de 768 px a 1023 px.
- **Viewport_Escritorio**: ancho de ventana de 1024 px o superior.
- **Área_Visible_Efectiva**: altura del área visible descontando las barras del navegador y del sistema operativo.
- **Navegadores_Objetivo**: las dos versiones estables más recientes de Chrome, Edge, Firefox y Safari de escritorio, más iOS Safari 16.4 o superior y Chrome para Android.

## Requisitos

### Requisito 1: Identidad visual propia

**Historia de usuario:** Como visitante del portal, quiero una página con identidad gráfica propia y reconocible, para percibir el proyecto como un trabajo profesional del colectivo y no como una plantilla genérica.

#### Criterios de Aceptación

1. EL Sistema_de_Diseño DEBERÁ declarar en un único archivo de tokens los valores de color, tipografía, espaciado, radios, sombras, duraciones y curvas que consume la Página_Principal, de modo que las hojas de estilo de la Página_Principal no contengan valores literales de color, espaciado, radio, sombra ni duración fuera de ese archivo.
2. EL Sistema_de_Diseño DEBERÁ definir una escala tipográfica de exactamente 7 niveles, con un tamaño base de 16 px, un nivel mínimo de 12 px o superior y una razón entre niveles consecutivos comprendida entre 1.2 y 1.333, con una variación máxima de ±0.01 entre esas razones.
3. EL Sistema_de_Diseño DEBERÁ definir una escala de espaciado de al menos 8 pasos, cada uno múltiplo entero de la unidad base de 4 px, que cubra de 4 px a 128 px.
4. EL Sistema_de_Diseño DEBERÁ definir una paleta de un color de acento primario y un color de acento secundario, ambos trazables a los activos de marca de Vendaval, más un máximo de 6 colores semánticos, cada uno acompañado del registro de su relación de contraste, expresada con dos decimales, sobre cada superficie donde se usa y cumpliendo los mínimos de contraste del Requisito 6.
5. LA Página_Principal DEBERÁ representar todos los iconos funcionales con un único conjunto de iconos vectoriales SVG cuyos trazos compartan un mismo grosor declarado como token y cuya caja de dibujo sea de 24 × 24 unidades en todos los iconos del conjunto.
6. MIENTRAS la ventana esté en el Viewport_Escritorio, LA Página_Principal DEBERÁ aplicar al menos 3 estructuras de retícula distintas entre sus secciones —dos estructuras son distintas cuando difieren en el número de columnas o en la proporción de anchos entre sus columnas—, sin que ninguna estructura se repita en dos secciones consecutivas.
7. EL Hero DEBERÁ presentar al menos un elemento gráfico vectorial derivado de los activos de marca de Vendaval (logotipo, patrón o composición tipográfica propios) que ocupe 20 % o más del área visible del Hero en el Viewport_Escritorio.
8. EL Sistema_de_Diseño DEBERÁ aplicar a los globos de información, los controles y las leyendas del Mapa_Interactivo los mismos tokens de color, tipografía, espaciado, radios y sombras que al resto de la Página_Principal, sin conservar los estilos visuales por defecto de la biblioteca de mapas.
9. LA Página_Principal DEBERÁ presentar cero caracteres con la propiedad Unicode Extended_Pictographic en los textos de interfaz declarados por el Proyecto —marcado, hojas de estilo y cadenas generadas por JavaScript—, excluyendo el contenido proveniente de los registros de datos de los espacios.
10. LA Página_Principal DEBERÁ aplicar al menos 3 tratamientos de superficie distintos entre sus secciones —un tratamiento queda definido por la combinación de tipo de fondo (relleno opaco, degradado lineal o patrón de marca), presencia de desenfoque de fondo y presencia de borde visible—, sin que ningún tratamiento se repita en dos secciones consecutivas ni ningún degradado se emplee en más de una sección.
11. LA Página_Principal DEBERÁ presentar cero elementos decorativos de resplandor radial difuso, en forma de esferas o halos, en los fondos de sus secciones.

### Requisito 2: Sistema de movimiento

**Historia de usuario:** Como visitante, quiero animaciones fluidas y con propósito, para entender la jerarquía del contenido sin que el movimiento estorbe la lectura.

#### Criterios de Aceptación

1. EL Sistema_de_Diseño DEBERÁ declarar al menos 3 tokens de duración con valores entre 120 ms y 600 ms y al menos 2 curvas de aceleración nombradas, y DEBERÁ ser la única fuente de los valores de duración y de curva que consumen las animaciones de entrada, las microinteracciones y las transiciones de la Página_Principal.
2. EL Motor_de_Movimiento DEBERÁ animar exclusivamente las propiedades `transform`, `opacity`, `filter` y `clip-path` en las animaciones de entrada, en las microinteracciones, en las animaciones continuas y en los efectos ligados al desplazamiento, sin animar propiedades que provoquen recálculo de diseño del documento.
3. CUANDO al menos 15 % del área de una sección o 120 px de su altura —lo que ocurra primero— quedan dentro del área visible por primera vez, incluida la primera pintura del documento, EL Motor_de_Movimiento DEBERÁ ejecutar su animación de entrada una sola vez por carga de página y completarla en 600 ms o menos, contando cualquier retardo de inicio.
4. CUANDO el visitante activa el estado de foco, de puntero encima o de presión sobre un control interactivo, EL Motor_de_Movimiento DEBERÁ iniciar el cambio de estado visual de ese control en 100 ms o menos desde el evento y completarlo en 200 ms o menos, con una diferencia observable entre la captura del estado de reposo y la del estado activo.
5. MIENTRAS Movimiento_Reducido esté activo, EL Motor_de_Movimiento DEBERÁ presentar todo el contenido en su estado final visible, con opacidad completa y sin desplazamiento, escala ni recorte residuales.
6. MIENTRAS Movimiento_Reducido esté activo, EL Motor_de_Movimiento DEBERÁ limitar las transiciones a cambios de opacidad de 100 ms o menos, sin animaciones continuas, sin efectos ligados al desplazamiento y sin desplazamientos animados.
7. MIENTRAS el visitante desplaza la Página_Principal de forma continua en el Dispositivo_de_Referencia durante una ventana de 5 s que recorra todas sus secciones, EL Motor_de_Movimiento DEBERÁ mantener una tasa media de 55 fotogramas por segundo o superior y un percentil 95 de duración de fotograma de 32 ms o menos, tomando la mediana de 3 repeticiones del recorrido.
8. SI un elemento aún no ha sido revelado por EL Motor_de_Movimiento, ENTONCES EL Motor_de_Movimiento DEBERÁ mantener el contenido textual de ese elemento presente en el DOM, incluido en el árbol de accesibilidad y con sus controles interactivos alcanzables por teclado.
9. CUANDO el visitante activa un enlace de sección de la Navegación_Principal, LA Página_Principal DEBERÁ ejecutar un desplazamiento animado de 600 ms o menos que finalice con el inicio de la sección de destino visible por debajo de la Navegación_Principal.
10. CUANDO el estado de Movimiento_Reducido cambia mientras la Página_Principal está abierta, EL Motor_de_Movimiento DEBERÁ aplicar el nuevo estado en 500 ms o menos, sin recargar el documento, conservando la posición de desplazamiento y dejando toda animación en curso en su estado final visible.
11. SI una sección ya revelada sale del área visible y vuelve a entrar, o si sale del área visible mientras su animación de entrada está en curso, ENTONCES EL Motor_de_Movimiento DEBERÁ mantener esa sección en su estado final visible sin repetir la animación de entrada.
12. MIENTRAS la pestaña esté oculta, EL Motor_de_Movimiento DEBERÁ mantener detenidas las animaciones continuas y los efectos ligados al desplazamiento, y conservar el estado visual alcanzado para retomarlo cuando la pestaña vuelva a ser visible.

### Requisito 3: Preservación de la funcionalidad existente

**Historia de usuario:** Como integrante del colectivo, quiero que el rediseño conserve todas las funciones actuales del portal, para no perder el trabajo ya validado en campo.

#### Criterios de Aceptación

1. EL Directorio DEBERÁ conservar la búsqueda por texto sobre nombre, dirección y municipio con coincidencia de subcadena que no distingue mayúsculas de minúsculas y admite términos de hasta 100 caracteres, los filtros de selección única de tipo de recinto, de municipio y de servicios de confort (agua potable, aire acondicionado y sombra alta) con una opción «todos» activa al cargar la Página_Principal, y el control que restablece el buscador y los tres filtros a esa opción.
2. EL Mapa_Interactivo DEBERÁ conservar las capas Recintos, Mapa de Calor con sus modos Isla de Calor y Confort & Sombra, Mapa de Agua con sus cuatro subfiltros (todos los puntos, bebedero, agua fría y garrafón) y Clima Actual con sus 11 estaciones municipales, manteniendo exactamente una capa activa en cada momento y presentando en la capa Recintos exactamente los espacios que el Directorio muestra con la búsqueda y los filtros vigentes, omitidos aquellos cuya latitud o longitud no sea un número.
3. EL Asistente_Climático DEBERÁ conservar la consulta de clima en vivo con temperatura y sensación térmica en grados Celsius, humedad relativa en porcentaje, índice UV con un decimal y viento en kilómetros por hora; el selector de las 11 estaciones municipales; la clasificación del índice UV en Bajo (menor que 3.0), Moderado (de 3.0 a menos de 6.0), Alto (de 6.0 a menos de 8.0) y Extremo (8.0 o más); la activación de GPS; y las tres acciones rápidas de búsqueda de agua, refugio climatizado y espacio techado, cada una de las cuales presenta el nombre y la distancia del espacio más cercano a la ubicación de referencia que cumple su criterio.
4. CUANDO el visitante selecciona una tarjeta del Directorio, LA Ficha_de_Espacio DEBERÁ presentar del espacio seleccionado su tipo de recinto, nombre, dirección, teléfono, municipio, los indicadores de agua potable, aire acondicionado y nivel de sombra, la descripción de confort climático y el enlace a su ubicación en Google Maps, y EL Mapa_Interactivo DEBERÁ centrarse en sus coordenadas y abrir su globo de información.
5. CUANDO el visitante envía el Formulario_de_Alta con nombre, dirección, tipo de recinto, municipio, latitud y longitud completos, EL Formulario_de_Alta DEBERÁ registrar el espacio en la base remota y presentarlo una sola vez en el Directorio y en el Mapa_Interactivo, tratando como el mismo espacio los registros con igual identificador o con latitud y longitud que difieran menos de 5×10⁻⁴ grados, y conservando como versión vigente la de la base remota.
6. PARA TODO par de coordenadas con latitud entre 21.6 y 22.5 grados y longitud entre −102.9 y −101.8 grados (extensión del estado de Aguascalientes), expresado con hasta 7 decimales, EL Extractor_de_Coordenadas DEBERÁ obtener el mismo par, con una tolerancia de 1×10⁻⁶ grados, a partir de la URL de Google Maps generada con ese par en cualquiera de los formatos admitidos: par precedido por arroba, par en un parámetro de consulta de la URL, par incrustado en los datos del lugar y par en texto simple separado por coma (propiedad de ida y vuelta).
7. SI la fuente de datos remota o la API de clima no responden en 10 segundos o devuelven un error, ENTONCES LA Página_Principal DEBERÁ presentar el catálogo local disponible (la lista base incorporada más los espacios guardados en el almacenamiento local del navegador) junto con un aviso de texto visible que identifique la fuente afectada y advierta que los datos mostrados pueden estar incompletos.
8. LA Página_Principal DEBERÁ conservar el contenido informativo actual: la identidad del colectivo, la colaboración con el IAJU, los 7 integrantes del Equipo cada uno con su nombre y su rol, y los enlaces del Pie_de_Página, cada uno con un destino existente dentro de la Página_Principal o entre los créditos de fuentes externas.
9. PARA TODA combinación de término de búsqueda y de filtros del Directorio, EL Directorio DEBERÁ presentar un resultado que sea subconjunto del Catálogo_Activo, idéntico para cualquier orden de aplicación de esos criterios y de tamaño menor o igual al del resultado que produce cualquier subconjunto de esos mismos criterios, e indicar con texto visible el número de espacios presentados, incluido el caso de cero espacios.
10. SI la base remota no acepta el registro enviado desde el Formulario_de_Alta, ENTONCES EL Formulario_de_Alta DEBERÁ conservar ese espacio en el almacenamiento local del navegador, presentarlo en el Directorio y en el Mapa_Interactivo con un aviso visible de sincronización pendiente, y dejar de presentar la copia local cuando el mismo espacio esté disponible en la base remota.
11. SI la URL entregada al Extractor_de_Coordenadas no contiene un par de coordenadas reconocible o el par obtenido queda fuera del rango del estado de Aguascalientes, ENTONCES EL Extractor_de_Coordenadas DEBERÁ no devolver ningún par, EL Formulario_de_Alta DEBERÁ conservar sin cambios los valores de latitud y longitud ya capturados y DEBERÁ presentar un aviso visible de que no se detectaron coordenadas válidas.

### Requisito 4: Estructura y jerarquía del contenido

**Historia de usuario:** Como visitante que llega por primera vez, quiero entender en pocos segundos qué ofrece el portal y qué puedo hacer, para decidir si me quedo.

#### Criterios de Aceptación

1. LA Página_Principal DEBERÁ presentar exactamente un encabezado de nivel 1 y una jerarquía en la que ningún encabezado posterior aumente más de un nivel respecto al encabezado que lo precede en el orden del documento.
2. EL Hero DEBERÁ presentar un título de entre 20 y 90 caracteres, un texto de apoyo de entre 80 y 220 caracteres, una acción primaria con texto visible que conduzca al Directorio y una acción secundaria con texto visible que conduzca al mapa oficial del proyecto en Google Maps e indique en su rótulo accesible que el destino es externo.
3. CUANDO el visitante abre la Página_Principal con un ancho dentro del Viewport_Móvil y un Área_Visible_Efectiva de 568 px de altura o superior, EL Hero DEBERÁ presentar el título completo, el texto de apoyo completo y el área táctil completa de la acción primaria dentro del área visible no ocupada por la Navegación_Principal, sin que el visitante desplace la página.
4. LA Página_Principal DEBERÁ incluir una sección de propósito del proyecto con un encabezado de nivel 2 y entre 2 y 3 pasos numerados, cada uno con un título de 40 caracteres o menos y una descripción de 160 caracteres o menos.
5. LA Página_Principal DEBERÁ ordenar sus secciones de arriba hacia abajo como Hero, propósito del proyecto, Asistente_Climático, Directorio, Mapa_Interactivo, Equipo y Pie_de_Página, con el mismo orden en el documento y en la presentación visual en el Viewport_Móvil, el Viewport_Tablet y el Viewport_Escritorio, y sin intercalar entre ellas ninguna otra sección con encabezado de nivel 2.
6. MIENTRAS el visitante recorre la Página_Principal, LA Navegación_Principal DEBERÁ señalar como activo exactamente un enlace: el de la sección enlazada desde la Navegación_Principal que ocupa la mayor proporción del área visible.
7. EL Pie_de_Página DEBERÁ mostrar en texto visible los créditos del colectivo, la colaboración con el IAJU y la atribución nominal de las fuentes de datos externas OpenStreetMap, CARTO y Open-Meteo, cada una con un enlace a su sitio de origen.
8. SI dos o más secciones enlazadas desde la Navegación_Principal ocupan la misma proporción del área visible, ENTONCES LA Navegación_Principal DEBERÁ señalar como activo el enlace de la sección que aparece primero en el orden establecido en el criterio 5.
9. CUANDO el desplazamiento de la Página_Principal se detiene, LA Navegación_Principal DEBERÁ reflejar el enlace activo correspondiente en 200 ms o menos.
10. MIENTRAS la ventana esté en el Viewport_Móvil, SI el Área_Visible_Efectiva tiene una altura menor que 568 px, ENTONCES EL Hero DEBERÁ presentar sin desplazamiento el título completo y el área táctil completa de la acción primaria.

### Requisito 5: Rendimiento

**Historia de usuario:** Como visitante con un teléfono de gama media y datos móviles, quiero que la página cargue y responda rápido, para consultar espacios y agua potable cuando estoy en la calle.

#### Criterios de Aceptación

1. CUANDO la Página_Principal se carga en el Dispositivo_de_Referencia sin caché de navegador ni almacenamiento previo del sitio (carga en frío), LA Página_Principal DEBERÁ alcanzar un Mayor Despliegue de Contenido (LCP) de 2.5 s o menos en al menos 4 de 5 cargas consecutivas de la misma versión publicada (percentil 75 de la serie).
2. CUANDO la Página_Principal se carga en frío en el Dispositivo_de_Referencia y se recorre hasta el Pie_de_Página, incluida la aparición del Mapa_Interactivo diferido, LA Página_Principal DEBERÁ mantener un Desplazamiento Acumulado de Diseño (CLS) de 0.1 o menos en al menos 4 de 5 recorridos consecutivos (percentil 75 de la serie).
3. CUANDO el visitante acciona los controles del Directorio o del Asistente_Climático (buscador, chips de filtro, selector de estación municipal y acciones rápidas), LA Página_Principal DEBERÁ mantener una Interacción hasta el Siguiente Despliegue (INP) de 200 ms o menos en el percentil 75 de una serie de 20 interacciones medidas en el Dispositivo_de_Referencia con el Mapa_Interactivo ya cargado.
4. EL Proceso_de_Compilación DEBERÁ entregar 180 KB o menos de JavaScript propio comprimido con Brotli en la carga inicial de la Página_Principal, entendida como los archivos de script solicitados desde la apertura del documento hasta la primera interacción del visitante, excluyendo las bibliotecas del Mapa_Interactivo.
5. MIENTRAS el Mapa_Interactivo permanezca a más de 300 px del área visible y el visitante no haya solicitado el Mapa_Interactivo, LA Página_Principal DEBERÁ posponer la descarga de la biblioteca de mapas, de sus complementos y de sus teselas.
6. LA Página_Principal DEBERÁ servir cada imagen en formato AVIF o WebP, con atributos de ancho y alto declarados, con un peso transferido de 200 KB o menos por imagen, con carga diferida para las imágenes situadas fuera del área visible inicial del Viewport_Móvil y sin carga diferida para la imagen que produce el Mayor Despliegue de Contenido.
7. LA Página_Principal DEBERÁ cargar como máximo 4 archivos de fuente tipográfica, con subconjunto latino, `font-display: swap` y un peso total transferido de 120 KB o menos.
8. MIENTRAS la pestaña esté oculta, LA Página_Principal DEBERÁ suspender la sincronización periódica de datos en segundo plano, sin emitir peticiones de sincronización hasta que la pestaña vuelva a ser visible.
9. CUANDO la Página_Principal inicia o reanuda la sincronización periódica de datos en segundo plano, LA Página_Principal DEBERÁ respetar un intervalo mínimo de 5 minutos entre el inicio de dos sincronizaciones consecutivas.
10. CUANDO la Página_Principal se abre con una dirección que apunta al Mapa_Interactivo o a un espacio concreto, LA Página_Principal DEBERÁ solicitar el paquete del Mapa_Interactivo sin esperar el desplazamiento del visitante y presentar el Mapa_Interactivo operable en 3 s o menos contados desde el Mayor Despliegue de Contenido, medido en el Dispositivo_de_Referencia en carga en frío.
11. SI la descarga del paquete del Mapa_Interactivo o de su primer conjunto de teselas falla o no se completa en 10 s, ENTONCES LA Página_Principal DEBERÁ mostrar en el espacio reservado del Mapa_Interactivo un aviso visible del estado del mapa con un control para reintentar la carga, y DEBERÁ conservar operables el Directorio y el Asistente_Climático con sus filtros activos sin recargar el documento.
12. EL Proceso_de_Compilación DEBERÁ entregar la carga inicial de la Página_Principal con un peso total transferido de 900 KB o menos comprimido, contando el documento, el CSS, el JavaScript, las fuentes y las imágenes del área visible inicial, y excluyendo el paquete diferido del Mapa_Interactivo y sus teselas.

### Requisito 6: Accesibilidad

**Historia de usuario:** Como persona que navega con teclado o con lector de pantalla, quiero operar todo el portal sin depender del ratón ni de la vista, para consultar espacios culturales en igualdad de condiciones.

#### Criterios de Aceptación

1. LA Página_Principal DEBERÁ presentar el texto normal —todo texto de tamaño menor a 24 px CSS, o menor a 18.66 px CSS con peso tipográfico 700 o superior— con una relación de contraste de 4.5:1 o superior respecto al color efectivo de la superficie compuesta que queda detrás de él, en los estados de reposo, puntero encima, foco y seleccionado.
2. LA Página_Principal DEBERÁ presentar el texto grande —todo texto de tamaño 24 px CSS o mayor, o de 18.66 px CSS o mayor con peso tipográfico 700 o superior—, los iconos funcionales y los bordes visibles de los controles con una relación de contraste de 3:1 o superior respecto al color efectivo de la superficie compuesta que queda detrás de ellos.
3. LA Página_Principal DEBERÁ permitir alcanzar con Tabulador y Mayús+Tabulador, accionar con Entrar en los enlaces y con Entrar o Barra espaciadora en los botones y en los controles de selección, y abandonar sin usar el ratón, todos sus controles interactivos, incluidos los chips de filtro, el conmutador de capas y los globos de información del Mapa_Interactivo, y las tarjetas del Directorio.
4. CUANDO un control recibe el foco de teclado, LA Página_Principal DEBERÁ mostrar un indicador de foco de 2 px CSS de grosor o más que rodee el perímetro del control con una relación de contraste de 3:1 o superior respecto a la superficie adyacente, y DEBERÁ mantener el control enfocado completamente visible, sin quedar cubierto por la Navegación_Principal ni por otro contenido persistente.
5. CUANDO se abre la Ficha_de_Espacio o el Formulario_de_Alta, o CUANDO la Ficha_de_Espacio abierta sustituye su contenido por el de otro espacio, LA Página_Principal DEBERÁ trasladar el foco de teclado al encabezado del diálogo resultante en 200 ms o menos.
6. MIENTRAS la Ficha_de_Espacio o el Formulario_de_Alta permanezcan abiertos, LA Página_Principal DEBERÁ mantener el foco de teclado dentro del diálogo abierto más reciente, recorriendo de forma cíclica sus elementos enfocables, e impedir que el foco alcance el contenido situado fuera de ese diálogo.
7. CUANDO se cierra la Ficha_de_Espacio o el Formulario_de_Alta, LA Página_Principal DEBERÁ devolver el foco al control que originó la apertura, o al encabezado de la sección que contenía ese control cuando este ya no esté presente o no sea enfocable.
8. CUANDO el visitante pulsa la tecla Escape mientras hay diálogos abiertos, LA Página_Principal DEBERÁ cerrar únicamente el diálogo abierto más reciente y conservar abiertos los diálogos restantes.
9. LA Página_Principal DEBERÁ ofrecer como primer elemento enfocable un enlace de salto al contenido principal que se vuelva visible al recibir el foco, y DEBERÁ presentar los demás elementos enfocables en un orden de tabulación que coincida con el orden visual de lectura de cada sección.
10. LA Página_Principal DEBERÁ estructurar el documento con una sola región de contenido principal, además de las regiones de encabezado, navegación y pie, y DEBERÁ distinguir con un nombre accesible propio cada región de navegación cuando exista más de una.
11. CUANDO el Asistente_Climático actualiza los valores de clima, LA Página_Principal DEBERÁ anunciar el cambio mediante una región dinámica de cortesía en 1 s o menos desde la actualización, sin trasladar el foco de teclado.
12. MIENTRAS la ventana esté en el Viewport_Móvil, LA Página_Principal DEBERÁ presentar los controles interactivos con un área táctil de 44 × 44 px CSS o superior y una separación de 8 px CSS o más entre áreas táctiles adyacentes.
13. SI un estado se comunica mediante color —incluidos el estado seleccionado de los chips de filtro y de las capas del Mapa_Interactivo—, ENTONCES LA Página_Principal DEBERÁ acompañar ese color con texto o con un icono que exprese el mismo estado, y DEBERÁ exponer ese estado a las tecnologías de asistencia como presionado o seleccionado.
14. LA Página_Principal DEBERÁ declarar en cada imagen informativa un texto alternativo de 1 a 150 caracteres que describa la información que aporta esa imagen, sin repetir el texto visible adyacente.
15. LA Página_Principal DEBERÁ marcar cada imagen decorativa como oculta para las tecnologías de asistencia y DEBERÁ excluirla del orden de tabulación.
16. LA Página_Principal DEBERÁ declarar en cada control interactivo, incluidos los que se representan únicamente con un icono, y en cada diálogo, un nombre accesible que identifique su función y que contenga íntegramente la etiqueta de texto visible del control cuando esta exista.
17. CUANDO el visitante activa o desactiva un chip de filtro, modifica la búsqueda o cambia un filtro del Directorio, LA Página_Principal DEBERÁ conservar el foco de teclado en el control accionado y anunciar el número de resultados obtenidos mediante una región dinámica de cortesía en 1 s o menos.
18. CUANDO el visitante amplía el texto hasta 200 % o aplica un zoom de página hasta 400 % sobre una ventana de 1280 × 1024 px CSS, LA Página_Principal DEBERÁ conservar visible y operable todo su contenido y todas sus funciones, sin recorte ni solapamiento de texto y sin requerir desplazamiento horizontal y vertical simultáneos, salvo en el Mapa_Interactivo.

> Nota: estos criterios corresponden a WCAG 2.2 nivel AA. La validación completa de conformidad requiere pruebas manuales con tecnologías de asistencia y revisión por una persona especialista en accesibilidad.

### Requisito 7: Comportamiento adaptable

**Historia de usuario:** Como visitante que entra desde el teléfono, la tablet o la computadora, quiero una composición adecuada a mi pantalla, para leer y operar sin molestias.

#### Criterios de Aceptación

1. PARA TODO ancho de área visible entre 320 px y 1920 px, en orientación vertical u horizontal, LA Página_Principal DEBERÁ presentar todo su contenido sin desplazamiento horizontal, de modo que ningún elemento sobresalga del ancho del área visible en más de 1 px.
2. MIENTRAS la ventana esté en el Viewport_Móvil, LA Página_Principal DEBERÁ presentar el contenido de cada sección en una sola columna, con márgenes laterales de 16 px o más medidos desde las áreas seguras que informa el dispositivo por muescas, esquinas redondeadas o barras del sistema.
3. MIENTRAS la ventana esté en el Viewport_Tablet, EL Directorio DEBERÁ presentar sus tarjetas en 2 columnas de igual ancho, con una separación de 16 px o más entre columnas.
4. MIENTRAS la ventana esté en el Viewport_Móvil, LA Navegación_Principal DEBERÁ presentar un menú desplegable que traslade el foco de teclado a su primer enlace al abrirse, confine el foco dentro del menú mientras permanezca abierto, impida el desplazamiento del contenido de fondo, ofrezca desplazamiento interno de sus enlaces cuando la altura de estos exceda la altura del área visible, y se cierre devolviendo el foco al control que lo abrió cuando el visitante elige un enlace, pulsa la tecla Escape o toca fuera del menú.
5. MIENTRAS la ventana esté en el Viewport_Escritorio, LA Página_Principal DEBERÁ limitar la medida de cada bloque de texto corrido de 2 líneas o más a un intervalo de 45 a 75 caracteres por línea.
6. MIENTRAS la ventana esté en el Viewport_Móvil, EL Mapa_Interactivo DEBERÁ ocupar una altura equivalente al 60 % de la altura del área visible, acotada a un mínimo de 240 px y un máximo de 480 px, y DEBERÁ mantener visibles dentro de esa altura sus controles de acercamiento y de capas, incluida la orientación horizontal con altura de área visible de 480 px o menor.
7. MIENTRAS la ventana esté en el Viewport_Escritorio, EL Mapa_Interactivo DEBERÁ ocupar una altura de entre 550 px y el 85 % de la altura del área visible.
8. DONDE el dispositivo no disponga de puntero fino, LA Página_Principal DEBERÁ exponer con un solo toque sobre el elemento la información que en el Viewport_Escritorio se muestra al colocar el puntero encima, y DEBERÁ ocultarla cuando el visitante toca fuera del elemento o pulsa la tecla Escape.
9. CUANDO el ancho del área visible cruza el límite entre dos de los viewports Viewport_Móvil, Viewport_Tablet y Viewport_Escritorio, LA Página_Principal DEBERÁ conservar, en 400 ms o menos y sin recargar el documento, el texto del buscador y los filtros activos del Directorio, la capa activa junto con el centro y el nivel de acercamiento del Mapa_Interactivo, y el espacio mostrado en la Ficha_de_Espacio si estaba abierta, presentándola en la posición que corresponde al nuevo viewport.
10. MIENTRAS la ventana esté en el Viewport_Escritorio, EL Directorio DEBERÁ presentar sus tarjetas en 3 columnas de igual ancho, con una separación de 24 px o más entre columnas.
11. MIENTRAS la ventana esté en el Viewport_Móvil o en el Viewport_Tablet, LA Ficha_de_Espacio DEBERÁ presentarse como panel inferior de ancho completo, con una altura de entre el 50 % y el 85 % de la altura del área visible, con el nombre del espacio y el control de cierre visibles en sus primeros 64 px superiores, con desplazamiento interno de su contenido y con su borde inferior por encima del área segura inferior que informa el dispositivo.
12. MIENTRAS la ventana esté en el Viewport_Escritorio, LA Ficha_de_Espacio DEBERÁ presentarse como panel lateral con un ancho de entre 360 px y 480 px que no exceda el 40 % del ancho del área visible, sin cubrir la Navegación_Principal y dejando visible el Mapa_Interactivo en el espacio restante.

### Requisito 8: Metadatos y posicionamiento

**Historia de usuario:** Como integrante del colectivo que difunde el portal en redes, quiero que el enlace se presente con título, descripción e imagen correctos, para que la difusión se vea cuidada.

#### Criterios de Aceptación

1. LA Página_Principal DEBERÁ declarar un único título de documento de entre 30 y 60 caracteres que contenga el nombre "Vendaval", con el mismo valor en `og:title` y en `twitter:title`.
2. LA Página_Principal DEBERÁ declarar una única descripción de entre 120 y 160 caracteres, con el mismo valor en la etiqueta de descripción del documento, en `og:description` y en `twitter:description`.
3. LA Página_Principal DEBERÁ declarar, con una sola aparición de cada etiqueta y sin valores vacíos, las etiquetas de Open Graph `og:type` con valor `website`, `og:site_name` con valor `Vendaval`, `og:locale` con valor `es_MX`, `og:title`, `og:description`, `og:url`, `og:image` con la URL absoluta de una imagen de vista previa de 1200 × 630 px existente en el resultado publicado, `og:image:width` con valor `1200`, `og:image:height` con valor `630` y `og:image:alt` con un texto de entre 40 y 120 caracteres que describa el contenido de esa imagen.
4. LA Página_Principal DEBERÁ declarar el idioma `es-MX` en el elemento raíz del documento y una única URL canónica absoluta con el mismo valor que `og:url`.
5. LA Página_Principal DEBERÁ incluir datos estructurados JSON-LD que se analicen sin errores como JSON válido, declaren el contexto `https://schema.org`, no contengan valores vacíos, y presenten un nodo de tipo `Organization` con nombre, URL absoluta del sitio y URL absoluta del logotipo, y un nodo de tipo `WebSite` con nombre, URL absoluta igual a la URL canónica, idioma `es-MX` y referencia al nodo `Organization` como responsable de la publicación.
6. EL Proceso_de_Compilación DEBERÁ publicar un archivo `robots.txt` que permita el rastreo de la Página_Principal y declare la URL absoluta del `sitemap.xml`, y un archivo `sitemap.xml` que se analice sin errores como XML válido y contenga la URL canónica de la Página_Principal con su fecha de última modificación.
7. LA Página_Principal DEBERÁ declarar, con archivos existentes en el resultado publicado, un favicon en formato SVG, un favicon alternativo en formato PNG de 32 × 32 px y una imagen de aplicación de 180 × 180 px.
8. LA Página_Principal DEBERÁ declarar, con una sola aparición de cada etiqueta y sin valores vacíos, las etiquetas de tarjeta de Twitter `twitter:card` con valor `summary_large_image`, `twitter:title`, `twitter:description`, `twitter:image` con la misma URL absoluta que `og:image` y `twitter:image:alt` con el mismo texto que `og:image:alt`.
9. CUANDO se genera el resultado publicado, EL Proceso_de_Compilación DEBERÁ derivar de un único valor de dominio base configurado en el despliegue todas las URLs absolutas de la URL canónica, de `og:url`, de `og:image`, de `twitter:image`, de los datos estructurados JSON-LD, del `robots.txt` y del `sitemap.xml`.
10. SI el valor de dominio base no está configurado al ejecutar una compilación de producción, ENTONCES EL Proceso_de_Compilación DEBERÁ terminar con un código de error y un mensaje que indique la configuración de dominio ausente, sin generar el resultado publicado.

### Requisito 9: Proceso de compilación y dependencias

**Historia de usuario:** Como persona que mantiene el sitio, quiero un proceso de compilación reproducible con presupuestos verificados, para publicar cambios sin degradar el rendimiento.

#### Criterios de Aceptación

1. EL Proceso_de_Compilación DEBERÁ producir un resultado formado únicamente por archivos estáticos (documentos HTML, hojas de estilo, JavaScript, imágenes, fuentes y datos), servible desde un servicio de archivos estáticos sin ejecución de código en el servidor y sin reglas de reescritura de rutas.
2. EL Proceso_de_Compilación DEBERÁ minificar todo el CSS y el JavaScript del resultado publicado y DEBERÁ incluir en el nombre de cada archivo de CSS, JavaScript, imagen y fuente generado una huella derivada de su contenido, de modo que dos contenidos distintos no compartan nombre.
3. EL Proyecto DEBERÁ declarar cada dependencia directa con una versión exacta, sin intervalos ni comodines, y DEBERÁ incluir en el repositorio el archivo de bloqueo que fija las versiones de todas las dependencias transitivas.
4. EL Proyecto DEBERÁ ofrecer un comando de desarrollo que sirva la Página_Principal y refleje cada cambio guardado en el código fuente en 2 s o menos sin recarga completa de la página, un comando de compilación para producción y un comando de verificación que ejecute las pruebas automatizadas y la comprobación de los límites de peso del Presupuesto_de_Rendimiento sobre los archivos generados.
5. SI una prueba automatizada falla o una compilación de producción supera cualquiera de los límites de peso del Presupuesto_de_Rendimiento verificables sobre los archivos generados (180 KB de JavaScript propio comprimido con Brotli excluyendo las bibliotecas del Mapa_Interactivo, 900 KB de carga inicial total, 4 archivos de fuente tipográfica y 120 KB de fuentes), ENTONCES EL Proceso_de_Compilación DEBERÁ terminar con un código de salida distinto de cero, informar cada comprobación fallida con su valor medido y su límite, y no entregar el resultado como publicable.
6. LA Página_Principal DEBERÁ obtener todo el código JavaScript y CSS de terceros, incluidos Leaflet, leaflet.heat y el cliente de Supabase, desde los archivos generados por EL Proceso_de_Compilación, con cero peticiones a redes de distribución externas para obtener código; las teselas de mapa y las llamadas a las API de datos quedan fuera de este límite.
7. EL Proceso_de_Compilación DEBERÁ leer el código fuente legible desde un directorio distinto del directorio del resultado publicado, DEBERÁ dejar los archivos del código fuente sin modificar y DEBERÁ vaciar el directorio del resultado antes de cada compilación de producción.
8. SI el registro público de paquetes no está disponible o una dependencia declarada deja de publicarse en él, ENTONCES EL Proceso_de_Compilación DEBERÁ completarse con las versiones registradas en el archivo de bloqueo y las dependencias ya instaladas en el Proyecto, o terminar con un código de salida distinto de cero indicando qué dependencia no pudo resolverse.
9. CUANDO EL Proceso_de_Compilación se ejecuta dos veces sobre el mismo estado del código fuente, el mismo archivo de bloqueo y las mismas variables de entorno, DEBERÁ producir el mismo conjunto de archivos, con los mismos nombres con huella de contenido y el mismo contenido byte a byte.
10. EL Proceso_de_Compilación DEBERÁ obtener las credenciales de acceso a la base remota y la URL pública del sitio de variables de entorno, y EL Proyecto DEBERÁ documentar el nombre de cada variable requerida sin incluir sus valores reales en el código fuente ni en el archivo de bloqueo.
11. SI al iniciar una compilación de producción falta una variable de entorno requerida o su valor está vacío, ENTONCES EL Proceso_de_Compilación DEBERÁ terminar con un código de salida distinto de cero antes de generar archivos, indicar el nombre de cada variable ausente y conservar sin cambios el resultado de la compilación anterior.

> Nota: los límites de peso del Presupuesto_de_Rendimiento (JavaScript propio, carga inicial total y fuentes) se comprueban de forma automática sobre los archivos generados; los límites de LCP, CLS e INP del Requisito 5 requieren medición en el Dispositivo_de_Referencia y no se verifican durante la compilación.

### Requisito 10: Compatibilidad y degradación

**Historia de usuario:** Como visitante con un navegador o dispositivo antiguo, quiero acceder a la información del portal, para consultar espacios culturales aunque no vea todos los efectos.

#### Criterios de Aceptación

1. LA Página_Principal DEBERÁ presentar, en cada uno de los Navegadores_Objetivo, las siete secciones enumeradas en el Requisito 4 y permitir completar sin errores las funciones enumeradas en el Requisito 3, incluidas la búsqueda y los filtros del Directorio, las cuatro capas del Mapa_Interactivo, el Asistente_Climático, la Ficha_de_Espacio y el Formulario_de_Alta.
2. SI una consulta de características en CSS o una comprobación de existencia en JavaScript, sin recurrir a la detección de agente de usuario, indica que el navegador carece de soporte para las animaciones ligadas al desplazamiento, ENTONCES LA Página_Principal DEBERÁ presentar todo el contenido de sus secciones en su estado final, con opacidad completa y sin desplazamiento ni escala residuales, y DEBERÁ mantener accionables todos sus controles interactivos.
3. SI una consulta de características en CSS indica que el navegador carece de soporte para el desenfoque de fondo, ENTONCES LA Página_Principal DEBERÁ presentar sus superficies con un relleno opaco que mantenga una relación de contraste de 4.5:1 o superior para el texto normal y de 3:1 o superior para el texto grande, los iconos funcionales y los bordes de los controles, conforme al Requisito 6.
4. SI el navegador tiene JavaScript deshabilitado, ENTONCES LA Página_Principal DEBERÁ mostrar el Hero, la sección de propósito, el Equipo, el Pie_de_Página, un enlace accionable al mapa oficial del proyecto en Google Maps y un aviso visible que indique que el Asistente_Climático, el Directorio y el Mapa_Interactivo requieren JavaScript.
5. SI el visitante rechaza el permiso de geolocalización, si la comprobación en JavaScript indica que el navegador no expone la API de geolocalización, o si la solicitud de ubicación no obtiene respuesta en 10 s, ENTONCES EL Asistente_Climático DEBERÁ conservar la estación municipal seleccionada con sus datos de clima, DEBERÁ mostrar un mensaje visible que indique el motivo entre permiso denegado, función no disponible o tiempo de espera excedido, y no DEBERÁ repetir la solicitud de ubicación de forma automática.
6. SI las comprobaciones de características indican que el navegador carece de alguna de las capacidades que requieren el Motor_de_Movimiento o el Mapa_Interactivo, caso de los navegadores ajenos a los Navegadores_Objetivo, ENTONCES LA Página_Principal DEBERÁ presentar el Hero, la sección de propósito, el listado del Directorio con nombre, dirección y municipio de cada espacio, el Equipo, el Pie_de_Página y el enlace accionable al mapa oficial del proyecto en Google Maps.
7. SI el navegador informa que no hay conexión de red al cargar la Página_Principal o durante la sesión, ENTONCES LA Página_Principal DEBERÁ mostrar un aviso visible del estado sin conexión, DEBERÁ mantener operables la búsqueda y los filtros del Directorio sobre el Catálogo_Activo disponible, y DEBERÁ posponer todo reintento de consulta remota hasta que se restablezca la conexión o hasta que el visitante lo solicite.
8. SI el almacenamiento local del navegador no está disponible o rechaza la escritura por falta de espacio, ENTONCES EL Formulario_de_Alta DEBERÁ conservar en pantalla los datos capturados por el visitante y DEBERÁ mostrar un mensaje visible que indique que el respaldo local no se guardó y si el registro en la base remota se completó.
