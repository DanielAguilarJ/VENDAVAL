```markdown
# VENDAVAL Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill teaches the core development patterns and conventions used in the VENDAVAL JavaScript codebase. You'll learn about file naming, import/export styles, commit message habits, and how to write and organize tests. While no frameworks are detected, the repository follows clear conventions to ensure consistency and maintainability.

## Coding Conventions

### File Naming
- Use **camelCase** for filenames.
  - Example: `userProfile.js`, `dataFetcher.js`

### Import Style
- Use **relative imports** for modules.
  - Example:
    ```javascript
    import { fetchData } from './dataFetcher';
    ```

### Export Style
- Use **named exports**.
  - Example:
    ```javascript
    // In dataFetcher.js
    export function fetchData() { ... }

    // In another file
    import { fetchData } from './dataFetcher';
    ```

### Commit Messages
- Freeform style, sometimes with prefixes.
- Average commit message length: ~74 characters.
- Example:
  ```
  Fix bug in dataFetcher when API returns empty response
  ```

## Workflows

### Adding a New Module
**Trigger:** When you need to add new functionality as a separate module.
**Command:** `/add-module`

1. Create a new file using camelCase, e.g., `newFeature.js`.
2. Implement your functions using named exports.
    ```javascript
    export function newFeature() { ... }
    ```
3. Import your module in other files using a relative path.
    ```javascript
    import { newFeature } from './newFeature';
    ```
4. Write a corresponding test file as `newFeature.test.js`.

### Writing and Running Tests
**Trigger:** When you add or update code and need to ensure correctness.
**Command:** `/run-tests`

1. Create a test file named `yourModule.test.js`.
2. Write tests according to the (unknown) test framework's syntax.
3. Run tests using the project's preferred method (check project docs or scripts).

### Committing Changes
**Trigger:** When you have made code changes and are ready to commit.
**Command:** `/commit-changes`

1. Write a clear, concise commit message (~74 chars).
2. Optionally use a prefix for clarity.
3. Commit your changes.

## Testing Patterns

- Test files follow the `*.test.*` naming convention, e.g., `dataFetcher.test.js`.
- The specific test framework is unknown; refer to existing test files for style.
- Place test files alongside or near the modules they test.

## Commands
| Command         | Purpose                                      |
|-----------------|----------------------------------------------|
| /add-module     | Scaffold and add a new module                |
| /run-tests      | Run all test files matching `*.test.*`       |
| /commit-changes | Commit code changes with a proper message    |
```