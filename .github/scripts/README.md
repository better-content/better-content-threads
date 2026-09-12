# Threads CI API providers

`provider-pins.json` lists the complete custom provider dependency graph. Entries with `sourceCommit` build that exact published source revision; entries without it retain the JAR from the explicitly pinned pack baseline. Dependency order is calculated from `dependsOn`, including HUD → Revival → Depth Director/Campaigns and Heat Sync → Latent Chemlib → Fixes.

`prepare-source-providers.py` checks baseline checkout provenance, pack index SHA-256 hashes, embedded source identity, source checkout commits/cleanliness, and JAR mod identity. Each source provider runs only its repository-local `stageRuntimeJar` task; Threads subsequently runs `verifyFull stageRuntimeJar` against the resulting `BC_CUSTOM_MOD_JAR_DIR`. A fresh output directory prevents stale API JARs being reused. `providers.json` records origin, source revision, and resulting hash. This is API preparation, with no deployment or pack-level work.

When a typed provider API changes, pin its verified and pushed full commit ID and update the relevant dependency edges. Keep the workflow baseline checkout revision equal to `baselineCommit`. Runtime outputs from source builds do not include the pack release's source annotation; source provenance therefore comes from the pinned clean checkout and the generated manifest, while baseline JARs retain their embedded annotation check.

Read-only local validation, without builds or copying JARs:

```sh
python3 .github/scripts/prepare-source-providers.py --pack-root PATH_TO_PINNED_PACK_CHECKOUT --validate-local /home/dev/mod_source
python3 -m unittest discover -s .github/scripts -p 'test_*.py'
```
