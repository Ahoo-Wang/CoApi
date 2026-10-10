# Contributing to CoApi

Thanks for helping improve CoApi! This guide describes how changes flow from an idea to a release.

## Issues first

- **Bugs**: open a [bug report](https://github.com/Ahoo-Wang/CoApi/issues/new?template=bug_report.yml) with the CoApi, Spring Boot and JDK versions and a minimal reproduction.
- **Features**: open a [feature request](https://github.com/Ahoo-Wang/CoApi/issues/new?template=feature_request.yml) describing the use case. Agree on the design in the issue before writing a large change.
- **Security vulnerabilities**: never in public issues, see [SECURITY.md](SECURITY.md).

## Branching and pull requests

- `main` is always releasable and protected: it changes only through pull requests that pass the required CI checks.
- Work on a short-lived branch named `<type>/<topic>`, e.g. `fix/token-refresh-margin`.
- Open a pull request against `main` and link the issue (`Closes #123`).
- **The PR title becomes the commit on `main`** (PRs are squash-merged), so it must follow
  [Conventional Commits](https://www.conventionalcommits.org/): `feat(starter): ...`, `fix: ...`, `docs: ...`.
  Mark breaking changes with `!`, e.g. `feat!: ...`. CI checks the title and labels the PR from its type and changed paths.
- Keep a PR focused on one change; split refactoring from behavior changes.
- Every PR gets an automated AI code review; address or answer its comments before merging.

## Local build

Requires JDK 17+.

```bash
./gradlew build                     # compile, test, detekt, license headers, coverage gate
./gradlew :spring:test              # one module
./gradlew detekt                    # static analysis (autocorrects formatting locally)
./gradlew codeCoverageReport codeCoverageVerification
./gradlew :spring:test -PtestJavaVersion=21   # run tests on another JDK
```

## Quality gates (CI)

Every PR must pass:

| Gate | Rule |
|------|------|
| Tests | `spring` and `spring-boot-starter` on JDK 17, 21 and 25; all examples |
| Static analysis | detekt + ktlint formatting (no autocorrect in CI), CodeQL |
| Compiler | Kotlin warnings are errors in library modules |
| License | every source file starts with the Apache 2.0 header (`config/detekt/license.template`) |
| Coverage | aggregated ≥ 95% line / ≥ 90% branch; Codecov: no project drop > 1%, ≥ 90% on new lines |
| PR title | Conventional Commits |

## Tests and documentation

- Add or update tests with every behavior change, and reproduce a bug with a failing test first.
- Use JUnit 5, `me.ahoo.test.asserts.assert` (fluent-assert) and MockK; use `ApplicationContextRunner` for Spring context tests.
- Tests must not depend on external services; use a local stand-in (see `ConsumerServerTest`).
- Read [Architecture](wiki/deep-dive/architecture.md) before changing core behavior: it lists the design rules and where each concern lives.
- User-visible changes update the wiki in **both** English (`wiki/`) and Chinese (`wiki/zh/`). Breaking changes extend the migration guide. Writing rules for the wiki are in [wiki/AGENTS.md](wiki/AGENTS.md).

## Releases

CoApi follows [Semantic Versioning](https://semver.org/): breaking changes bump the major version, features the minor, fixes the patch.

1. Open a pull request titled `release: vX.Y.Z` that bumps `version` in `gradle.properties`, and merge it once CI passes.
2. Create the GitHub release `vX.Y.Z` with release notes (upgrading notes first).
3. The release triggers publishing to Maven Central and GitHub Packages.

## License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE).
