# Git Workflow (for the three of us)

Simple rules, so we never overwrite each other's work. We use **NetBeans' built-in Git** and one branch, `main`.

## Who edits what

Most conflicts happen when two people edit the same file. We split the repo like this:

| Folder or file | Owner |
|---|---|
| `AngHulingTanod/src/**/*.java` | Gabriel |
| Art files (the assets/resources folder) | Jaron |
| `README.md`, `CREDITS.md`, UML, proposal | Jaron |
| `docs/04-pitch-and-demo.md`, `docs/06-playtest-log.md`, slides | Gio |
| `docs/01` to `03`, `docs/05` | Gabriel (everyone may suggest changes in chat first) |
| Data files (ship stats, waves, maps) | Gabriel and Gio. Tell each other before editing |

Need a change in someone else's file? Message them. Don't edit it yourself.

## Daily routine

**Before you start:** pull the latest.
> Right-click the project > **Git > Remote > Pull from Upstream** (or **Pull...**, choose `origin`, `main`).

**While you work:** commit small and often, one idea per commit.
> Right-click the project > **Git > Commit...**

**When you finish:** push.
> Right-click the project > **Git > Remote > Push...**, `origin`, `main`.

Pull first, push last. If you forget to pull and then push, Git will reject it. Just pull, then push again.

## Good commit messages

Write what changed, in plain words, starting with a verb:

- `Add tile map loader`
- `Fix beam not clearing at dawn`
- `Add Jaron's sprite sheets for water and island`

Avoid: `update`, `stuff`, `final`, `fix`.

## What to commit and what not to

**Commit:** source (`.java`), data files, art, docs, `build.xml`, `nbproject/project.xml`, `nbproject/project.properties`.

**Never commit:** the `build/` and `dist/` folders, `nbproject/private/`, and secrets such as your personal access token.
The `.gitignore` already blocks these. If you see them in the commit window, untick them.

Exception: at final submission we **do** put the finished `AngHulingTanod.jar` into a `release/` folder, on purpose.

## If you get a merge conflict

1. Don't panic. Nothing is lost.
2. NetBeans marks the file with conflict markers. Open it: **Git > Resolve Conflicts...**
3. Keep the right lines. If unsure, ask the other person *before* choosing.
4. Commit, then push.

If it gets messy, copy your folder somewhere safe first, then ask Gabriel.

## Authentication

GitHub does not accept your account password in NetBeans. Use your username and a **personal access token** (classic,
with the `repo` permission) as the password. Never share the token or put it in a file.

## Release checklist (Nov 29 to 30)

- [ ] Everyone pulls, and the game still runs
- [ ] Gabriel checks the Main Class is set (project **Properties > Run**), then builds the JAR (right-click project >
      **Clean and Build**; the JAR appears in the `dist/` folder)
- [ ] The JAR runs on a different computer
- [ ] JAR copied to `release/` and committed
- [ ] README, CREDITS, UML, and the playtest log are final
- [ ] Tag the commit: `final-submission`
- [ ] No new commits after the Nov 30 submission
