# AGENTS.md: wiki

The CoApi user documentation, built with VitePress and deployed to https://coapi.ahoo.me on every push to `main` that touches `wiki/` (`.github/workflows/deploy-wiki.yml`). PRs build it in `wiki-test.yml`.

## Commands

```bash
cd wiki
pnpm install
pnpm dev       # http://localhost:5173
pnpm build     # fails on dead internal links
```

## Layout

The sidebar is defined once in `.vitepress/config.mts` (`sidebar()`), with labels per locale. URLs are public and linked from outside, so do not rename or move pages without a strong reason.

| Group | Pages |
|-------|-------|
| Getting Started | `getting-started/overview`, `installation`, `quick-start` |
| Guide | `deep-dive/annotations` (Defining Clients), `auto-configuration` (Registering Clients), `client-modes`, `load-balancing`, `customization`, `authentication`, `examples` |
| Reference | `getting-started/configuration`, `troubleshooting`, `migration-v3`, `deep-dive/architecture` |

`zh/` mirrors every page. Adding a page means adding it in both languages and to `sidebar()` in `config.mts`.

## Writing rules

- **The code is the source of truth.** Verify every claim against `api/`, `spring/` and `spring-boot-starter/` before writing it. If code and docs disagree, fix the docs, or report the bug.
- **One fact, one page.** Link instead of repeating. Property semantics live in `configuration.md`, error messages in `troubleshooting.md`, internals in `architecture.md`.
- **Keep EN and zh in sync** in the same change: same headings, same examples.
- No hard-coded CoApi versions (use `<version>` and link to the latest release). No line-number source links: they rot. Link to files on `main`.
- Frontmatter `title` and `description` on every page. Quote a description that starts with `@`.
- Mermaid only where a diagram explains more than a table. Don't hard-code colors (the theme switches light/dark). Avoid `<...>` in Mermaid labels.
- Escape Kotlin placeholders as `\${...}` in Kotlin samples. Never write `{{` outside code blocks (Vue interpolation).
