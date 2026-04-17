# Branch Governance

This repository maintains two formal delivery branches:

- `sb3`: Spring Boot 3 stable line
- `sb4`: Spring Boot 4 / Jackson 3 upgrade line

`master` should be treated as a historical baseline instead of the default working branch for day-to-day fixes.

## Current branch intent

- `sb3` is the branch for stable fixes, business fixes, and changes that may need to be released before the SB4 migration is complete.
- `sb4` is the branch for Spring Boot 4 compatibility, Jackson 3 migration, Jakarta alignment, and related Camunda 7 fork work.

## Delivery rules

1. Common fixes must land on `sb3` first.
2. After validation, common fixes may be cherry-picked to `sb4`.
3. SB4-only compatibility work must stay on `sb4`.
4. Do not merge `sb4` back into `sb3`.
5. Do not use `master` as the default PR base unless there is an explicit governance decision to do so.

## Git safety rules

1. Check branch and worktree state before editing:

```bash
git branch --show-current
git status --short --branch
```

2. Prefer fetching remote state instead of pulling blindly:

```bash
git fetch --all --prune
```

3. If the worktree is dirty:
- do not run `git pull`
- do not switch branches casually
- do not use destructive reset commands
- prefer `git worktree` for isolated cross-branch work

## PR guidance

- Stable bugfix PRs should target `sb3`.
- Upgrade and compatibility PRs should target `sb4`.
- `master` should not receive routine feature or compatibility PRs.

## Why this exists

Without explicit branch governance, automated coding tools can easily:

- start work from the wrong base branch
- mix SB3 and SB4 changes in the same commit
- open PRs against the wrong target branch
- pull or switch branches in a dirty worktree and corrupt in-progress work

These rules are intended to keep SB3 and SB4 evolving in parallel without cross-line contamination.
