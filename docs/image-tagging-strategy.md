# Image Tagging Strategy

## Objective

Define a clear and consistent tagging strategy for the Docker images published to GitHub Container Registry (GHCR), so that integration, releases, and production deployments are predictable and traceable.

## Official image name

The official image name for this project is:

`ghcr.io/lcapi/multy-bot`

This is the canonical container image reference that all workflows and deployments must use.

## Integration tags

Images published from the `main` branch must use these tags:

- `main`
- `sha-<commit>`

### Meaning

#### `main`
Represents the latest image built from the current state of the `main` branch.

Example:

`ghcr.io/lcapi/multy-bot:main`

#### `sha-<commit>`
Represents an image tied to a specific Git commit.

Example:

`ghcr.io/lcapi/multy-bot:sha-a1b2c3d`

This tag provides traceability and allows a build to be linked to an exact repository state.

## Release tags

Images published from Git release tags must use semantic versioning:

- `vX.Y.Z`

Examples:

- `ghcr.io/lcapi/multy-bot:v0.1.0`
- `ghcr.io/lcapi/multy-bot:v1.0.0`

### Rules

- Release images are only created from Git tags that follow the `vX.Y.Z` format.
- These tags represent stable, versioned artifacts intended for formal releases.
- Production deployments should use these versioned tags whenever possible.

## `latest` policy

The `latest` tag is optional.

### Decision

For this project:

- `latest` is not required
- `latest` must not be the default reference for production deployments

### Reason

The `latest` tag is not precise enough for reliable operations because:

- it is not tied to a clearly visible version
- it makes rollback harder
- it makes troubleshooting less explicit
- it can change without a deployment manifest changing visibly

For these reasons, production should rely on explicit version tags such as `v0.1.0`.

## Deployment policy

### Integration / non-production environments

May use:

- `ghcr.io/lcapi/multy-bot:main`

or, if exact traceability is needed:

- `ghcr.io/lcapi/multy-bot:sha-<commit>`

### Production

Should use:

- `ghcr.io/lcapi/multy-bot:vX.Y.Z`

Production must avoid using mutable tags such as `main` or `latest` as the primary deployment reference.

## Workflow expectations

### Push to `main`

The workflow responsible for publishing integration images must publish:

- `main`
- `sha-<commit>`

### Release workflow

The workflow responsible for formal releases must publish:

- `vX.Y.Z`

It may optionally publish `latest` in the future, but this is not part of the current strategy.

## Summary

### Official image

`ghcr.io/lcapi/multy-bot`

### Integration tags

- `main`
- `sha-<commit>`

### Release tags

- `vX.Y.Z`

### `latest`

- optional
- not used as production baseline

### Production recommendation

Deploy fixed version tags such as `v0.1.0`.

## Future considerations

This strategy may evolve later to include:

- a `latest` tag for the most recent stable release
- environment-specific tags
- multi-architecture image tags
- additional metadata labels for traceability