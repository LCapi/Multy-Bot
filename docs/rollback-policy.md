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
  Rule

## Rule
Rollback must always target an existing, previously validated release tag.

Examples:
•	v0.1.0
•	v0.2.0

# Production rule

Production deployments must use fixed version tags.

Rollback must never rely on mutable tags such as:
•	main
•	latest

# Future improvements

Possible future enhancements include:
•	automated rollback
•	approval gates before production deployment
•	health-based rollback triggers