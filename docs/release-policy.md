# Release Policy

## Objective

Define how the project is versioned, how releases are created, and when a version is considered ready for production.

## Versioning

This project uses **Semantic Versioning (SemVer)**:

`MAJOR.MINOR.PATCH`

Examples:

- `0.1.0`
- `0.1.1`
- `1.0.0`

### Meaning

- **MAJOR**: incompatible or breaking changes
- **MINOR**: new backward-compatible functionality
- **PATCH**: backward-compatible bug fixes

## Branching model

This project uses a simple branch model:

- **`main`** is the main integration branch
- all changes must arrive through **Pull Requests**
- direct pushes to `main` are restricted by branch protection rules

### Working flow

1. Create a branch from `main`
2. Implement the change
3. Open a Pull Request against `main`
4. Wait for CI to pass
5. Merge into `main`

## Release tags

Formal releases are created using Git tags following this format:

`vX.Y.Z`

Examples:

- `v0.1.0`
- `v0.2.3`
- `v1.0.0`

Only tags matching this format are considered release tags.

## Who creates tags

Release tags must be created by a project maintainer.

In this project, tags are created manually when the state of `main` is considered stable and ready for release.

## Release process

A release is created from a stable commit already merged into `main`.

### Release steps

1. Ensure `main` is green in CI
2. Confirm the target version number
3. Create the Git tag using SemVer format
4. Push the tag to the remote repository
5. Let the release workflow:
    - build the project
    - publish the versioned Docker image to GHCR
    - create the GitHub Release

## Production policy

A version is promoted to production only when:

- it has been merged into `main`
- CI is green
- a release tag `vX.Y.Z` has been created
- the corresponding versioned image has been published successfully

### Production deployment rule

Production must deploy **fixed version tags** such as:

`ghcr.io/lcapi/multy-bot:v0.1.0`

Production must not rely on mutable tags such as:

- `main`
- `latest`

## Image tagging reference

### Integration tags

Published from `main`:

- `main`
- `sha-<commit>`

### Release tags

Published from release tags:

- `vX.Y.Z`

## Summary

- Versioning uses **SemVer**
- Development flows through **feature branches + Pull Requests**
- `main` is the protected integration branch
- Releases are created with tags in format **`vX.Y.Z`**
- Tags are created manually by the maintainer
- Production uses **versioned release images**, not mutable tags

## Example

A normal release flow looks like this:

1. Changes are merged into `main`
2. CI passes
3. The maintainer creates tag `v0.1.0`
4. The release workflow publishes:
    - `ghcr.io/lcapi/multy-bot:v0.1.0`
5. That version becomes eligible for production deployment