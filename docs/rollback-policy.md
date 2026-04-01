# Rollback Policy

## Objective

Define a simple and reliable rollback procedure in case a release breaks production.

## Rollback strategy

This project uses **version-based rollback**.

Rollback is performed by re-deploying the last known stable release image.

Example:

`ghcr.io/lcapi/multy-bot:v0.2.0`

## Why this approach

This strategy is preferred over re-tagging because it:

- preserves traceability
- keeps release history clear
- avoids changing the meaning of existing tags
- makes the production state explicit

## When to rollback

Rollback should be considered if a newly deployed release causes:

- service startup failures
- unstable runtime behavior
- major functional regression
- production degradation