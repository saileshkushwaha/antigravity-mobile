package com.example.antigravity.model

object SkillsCatalog {

    val allDesktopSkills: List<SkillItem> = listOf(
        // ==========================================
        // 1. Core & Architecture
        // ==========================================
        SkillItem(
            name = "antigravity-guide",
            description = "Authoritative guide and quick reference for Google Antigravity (AGY), CLI, Antigravity 2.0, slash commands, keybindings, and customizations.",
            category = "Core",
            isEnabled = true
        ),
        SkillItem(
            name = "google-antigravity-sdk",
            description = "Design, implement, and debug autonomous AI agents and multi-agent systems using the Google Antigravity (AGY) SDK.",
            category = "Core",
            isEnabled = true
        ),
        SkillItem(
            name = "agy-customizations",
            description = "Comprehensive guide for Antigravity Customization System, loading priority, skills, rules, plugins, hooks, and MCP servers.",
            category = "Core",
            isEnabled = true
        ),
        SkillItem(
            name = "workflow-skill-creator",
            description = "Distills a completed user workflow or interaction into a reusable agent skill.",
            category = "Core",
            isEnabled = true
        ),
        SkillItem(
            name = "migrate-workflows",
            description = "Automatically migrate legacy workflows to modern skills across global and workspace configurations.",
            category = "Core",
            isEnabled = true
        ),
        SkillItem(
            name = "generative_ui",
            description = "Render rich interactive HTML widgets inline in the chat or as standalone artifacts.",
            category = "Core",
            isEnabled = true
        ),
        SkillItem(
            name = "credentials",
            description = "Instructions for handling API keys and credentials safely, verifying presence, and prompting user securely.",
            category = "Core",
            isEnabled = true
        ),
        SkillItem(
            name = "clean-architecture",
            description = "Domain-driven design, separation of concerns into Presentation, Domain, Data layers, and SOLID design patterns.",
            category = "Architecture",
            isEnabled = true
        ),
        SkillItem(
            name = "refactoring-engine",
            description = "Autonomous code smells detection, complexity reduction, anti-pattern mitigation, and modernization.",
            category = "Architecture",
            isEnabled = true
        ),
        SkillItem(
            name = "code-review-standards",
            description = "Rigorous automated peer review, lint compliance, memory safety, and branch protection enforcement.",
            category = "Architecture",
            isEnabled = true
        ),

        // ==========================================
        // 2. Web & Modern Frontend
        // ==========================================
        SkillItem(
            name = "modern-web-guidance",
            description = "Modern web development best practices (modals, popovers, CSS grid, view transitions, CWV, modern JS).",
            category = "Web & Frontend",
            isEnabled = true
        ),
        SkillItem(
            name = "chrome-devtools",
            description = "Uses Chrome DevTools via MCP for debugging, troubleshooting, performance analysis, and browser automation.",
            category = "Web & Frontend",
            isEnabled = true
        ),
        SkillItem(
            name = "chrome-extensions",
            description = "Build and publish Chrome Extensions using Manifest V3 best practices (popup, service worker, declarativeNetRequest).",
            category = "Web & Frontend",
            isEnabled = true
        ),
        SkillItem(
            name = "debug-optimize-lcp",
            description = "Guides debugging and optimizing Largest Contentful Paint (LCP) and Core Web Vitals using DevTools MCP.",
            category = "Web & Frontend",
            isEnabled = true
        ),
        SkillItem(
            name = "memory-leak-debugging",
            description = "Diagnoses and resolves memory leaks in JavaScript/Node.js applications and heap snapshot analysis.",
            category = "Web & Frontend",
            isEnabled = true
        ),
        SkillItem(
            name = "a11y-debugging",
            description = "Uses Chrome DevTools MCP for accessibility (a11y) debugging, focus states, keyboard navigation, and contrast.",
            category = "Web & Frontend",
            isEnabled = true
        ),
        SkillItem(
            name = "troubleshooting",
            description = "Uses Chrome DevTools MCP and documentation to troubleshoot connection, target, and server issues.",
            category = "Web & Frontend",
            isEnabled = true
        ),

        // ==========================================
        // 3. Android & Mobile
        // ==========================================
        SkillItem(
            name = "android-cli",
            description = "Commands and instructions for android CLI: project creation, emulator management, APK runs, and UI inspection.",
            category = "Mobile",
            isEnabled = true
        ),
        SkillItem(
            name = "compose-performance",
            description = "Optimize Jetpack Compose recomposition, memory retention, derivedStateOf, and 60fps UI frame rates.",
            category = "Mobile",
            isEnabled = true
        ),
        SkillItem(
            name = "android-security",
            description = "Android Keystore hardware-backed encryption, biometric auth, and Network Security Config.",
            category = "Mobile",
            isEnabled = true
        ),
        SkillItem(
            name = "a11y-auditing",
            description = "Accessibility testing, WCAG 2.1 compliance, talkback semantics, and minimum 48dp touch targets.",
            category = "Mobile",
            isEnabled = true
        ),
        SkillItem(
            name = "xcode-project-setup",
            description = "Safely modifies Xcode projects (.pbxproj) to add Swift Packages and link files for iOS.",
            category = "Mobile",
            isEnabled = true
        ),

        // ==========================================
        // 4. Flutter & Dart Ecosystem
        // ==========================================
        SkillItem(
            name = "flutter-apply-architecture-best-practices",
            description = "Architects a Flutter application using the recommended layered approach (UI, Logic, Data).",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-fix-layout-issues",
            description = "Fixes Flutter layout errors (overflows, unbounded constraints) using Dart and Flutter MCP tools.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-build-responsive-layout",
            description = "Uses LayoutBuilder, MediaQuery, and Expanded/Flexible to create layouts adapting across form factors.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-add-widget-test",
            description = "Implement component-level tests using WidgetTester to verify UI rendering and user interactions.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-add-widget-preview",
            description = "Adds interactive widget previews to the project using the previews.dart system.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-add-integration-test",
            description = "Configures Flutter Driver for app interaction and permanent integration tests.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-setup-declarative-routing",
            description = "Configure MaterialApp.router using go_router for URL-based navigation and deep linking.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-setup-localization",
            description = "Add flutter_localizations, intl, and l10n.yaml configuration for multi-language support.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-implement-json-serialization",
            description = "Create model classes with fromJson and toJson methods using dart:convert.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "flutter-use-http-package",
            description = "Use the http package to execute GET, POST, PUT, DELETE REST API requests.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-run-static-analysis",
            description = "Execute dart analyze to identify warnings/errors and dart fix --apply to resolve mechanical lints.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-add-unit-test",
            description = "Write and organize unit tests for functions, methods, and classes using package:test.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-fix-runtime-errors",
            description = "Fetches active stack traces, locates failing lines, applies fixes, and verifies via hot reload.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-generate-test-mocks",
            description = "Define and generate mock objects for external dependencies using mockito and build_runner.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-setup-ffi-assets",
            description = "Compiles and packages C/C++ source into dynamic or static libraries using Native Assets hooks.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-use-ffigen",
            description = "Automatically generate FFI bindings for C/Objective-C/Swift integrations instead of manual setups.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-use-pattern-matching",
            description = "Use modern switch expressions and pattern matching for clean state branching.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-use-primary-constructors",
            description = "Write syntactically correct primary constructors and migrate to concise constructor syntax.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-write-documentation",
            description = "Formatting guidelines for writing Dart /// API documentation following Effective Dart.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-collect-coverage",
            description = "Collect test coverage using package:coverage and generate LCOV reports.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-build-cli-app",
            description = "Entrypoint structure, exit codes, and cross-platform scripts for Dart command-line tools.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-migrate-to-checks-package",
            description = "Replace package:matcher expect calls with package:checks equivalents.",
            category = "Flutter & Dart",
            isEnabled = true
        ),
        SkillItem(
            name = "dart-resolve-package-conflicts",
            description = "Workflow for fixing package version conflicts when pub get fails.",
            category = "Flutter & Dart",
            isEnabled = true
        ),

        // ==========================================
        // 5. Gemini & AI Models
        // ==========================================
        SkillItem(
            name = "gemini-api-dev",
            description = "Building applications with Gemini API hosted models (Gemini 2.5 Flash, Pro, Gemma 4), multimodal, tools, and structured outputs.",
            category = "AI & ML",
            isEnabled = true
        ),
        SkillItem(
            name = "gemini-interactions-api",
            description = "Interactions API for text generation, multi-turn chat, multimodal reasoning, and background tasks.",
            category = "AI & ML",
            isEnabled = true
        ),
        SkillItem(
            name = "gemini-live-api-dev",
            description = "Real-time, bidirectional audio/video/text WebSocket streaming with the Gemini Live API.",
            category = "AI & ML",
            isEnabled = true
        ),
        SkillItem(
            name = "gemini-omni-flash-api",
            description = "Generative video editing, image-to-video transitions, and video extensions using Gemini Omni 1.1 Flash.",
            category = "AI & ML",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-ai-logic-basics",
            description = "Official integration for Firebase AI Logic (Gemini API) into web and mobile apps.",
            category = "AI & ML",
            isEnabled = true
        ),
        SkillItem(
            name = "open-model-gateways",
            description = "Unified routing across OpenRouter, Groq LPU, Ollama local, and Hugging Face.",
            category = "AI & ML",
            isEnabled = true
        ),
        SkillItem(
            name = "prompt-engineering",
            description = "Structured JSON output generation, chain-of-thought prompting, and few-shot reasoning.",
            category = "AI & ML",
            isEnabled = true
        ),

        // ==========================================
        // 6. Cloud & Firebase Infrastructure
        // ==========================================
        SkillItem(
            name = "firebase-basics",
            description = "Firebase CLI setup, project initialization, google-services.json, and environment provisioning.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-firestore",
            description = "Sets up, manages, and executes queries against Cloud Firestore database instances with security rules.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-auth-basics",
            description = "User sign-in, authentication providers, user management, and token security rules.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-hosting-basics",
            description = "Deploy static web apps, Single Page Apps (SPAs), and microservices to Firebase Hosting.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-app-hosting-basics",
            description = "Deploy and manage full-stack web apps (Next.js, Angular) with backend compute on App Hosting.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-remote-config-basics",
            description = "Remote Config template management, feature flags, and dynamic client behavior updates.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-crashlytics",
            description = "Crash reporting, provisioning, and Crashlytics SDK integration for Android and iOS.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-data-connect",
            description = "Builds and deploys Firebase Data Connect (SQL Connect) PostgreSQL backends with type-safe SDKs.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "firebase-security-rules-auditor",
            description = "Evaluates security rules for Cloud Firestore to ensure robust, zero-leak access policies.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "gcloud-auth-verification",
            description = "Identifies and resolves missing Google Cloud authentication and Application Default Credentials (ADC).",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "gcs-security-assessment",
            description = "Assesses security posture, risk evaluation, and SAIF compliance for Google Cloud Storage buckets.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "accidental-data-loss-prevention",
            description = "Stop-and-verify safeguards before running irreversible deletion commands on DBs and storage buckets.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),
        SkillItem(
            name = "docker-containers",
            description = "Multi-stage container builds, microservices topology, and container sandboxing.",
            category = "Cloud & Firebase",
            isEnabled = true
        ),

        // ==========================================
        // 7. Data Engineering & BigQuery
        // ==========================================
        SkillItem(
            name = "bigquery-sql",
            description = "Query optimization techniques, execution best practices, and performance tuning rules for BigQuery.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "bigquery-ai-ml",
            description = "BigQuery built-in machine learning, time-series forecasting, anomaly detection, and GenAI SQL functions.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "bigquery-bigframes",
            description = "BigQuery DataFrames (BigFrames) Python pandas/scikit-learn-style API for scalable dataframes.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "bigquery-data-transfer-service",
            description = "Discovers and inspects BigQuery Data Transfer Service (DTS) ingestion pipelines.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "bigquery-graph",
            description = "Defines and queries property graphs and semantic graphs in BigQuery using Graph Query Language (GQL).",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "building-data-apps",
            description = "Builds modern data dashboards and interactive reports using React + Vite or Streamlit over GCP.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "data-autocleaning",
            description = "Automated data quality and transformation capabilities for Dataform/dbt/BigQuery pipelines.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "dataform-bigquery",
            description = "Clean, correct, and efficient Dataform SQLX pipeline code for BigQuery ELT transformations.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "dbt-bigquery",
            description = "Creating, modifying, and optimizing dbt pipelines, models, and incremental tables for BigQuery.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "discovering-gcp-data-assets",
            description = "Finds and inspects data assets (datasets, tables, views, BigLake) within Google Cloud.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "enforcing-resource-attribution",
            description = "Enforces resource attribution, project headers, and cost labels for bq and gcloud CLI runs.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "federate-lakehouse-catalog",
            description = "Sets up Google Cloud Lakehouse federated catalogs to remote Iceberg REST catalogs (Databricks, Glue).",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "gcp-data-pipelines",
            description = "Orchestration entry point guiding between dbt, Beam/Dataflow, Dataform, and Dataproc Spark.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "gcp-dataflow",
            description = "Writing, packaging, executing, and troubleshooting Apache Beam pipelines and Flex Templates on Dataflow.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "gcp-managed-airflow-migrations",
            description = "Migrating Airflow DAGs in Managed Service for Apache Airflow (MSAA / Cloud Composer).",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "gcp-pipeline-orchestration",
            description = "Generates and updates orchestration pipelines for Cloud Composer to schedule multi-step jobs.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "gcp-pipeline-resource-provisioning",
            description = "Declarative infrastructure provisioning via deployment.yaml for BigQuery and DTS.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "gcp-spark",
            description = "Develops and executes Apache Spark ETL pipelines and ML models on Dataproc Serverless and clusters.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "gcp-composer-troubleshooting",
            description = "Generates Root Cause Analysis (RCA) and troubleshooting for failed Cloud Composer DAGs.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "managing-python-dependencies",
            description = "Dependency management adhering to project-specific virtualenvs avoiding global pip installs.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "ml-best-practices",
            description = "Best practices for regression, classification, clustering, statistical testing, and model comparison.",
            category = "Data & BigQuery",
            isEnabled = true
        ),
        SkillItem(
            name = "notebook-guidance",
            description = "Jupyter notebook data exploration, %%bqsql magics, data visualization, and ML workflow validation.",
            category = "Data & BigQuery",
            isEnabled = true
        ),

        // ==========================================
        // 8. DevOps & SDLC Operations
        // ==========================================
        SkillItem(
            name = "github-actions-ci",
            description = "Automated multi-stage APK builds, test matrices, and dependency caching.",
            category = "DevOps & SDLC",
            isEnabled = true
        ),
        SkillItem(
            name = "multi-env-deploy",
            description = "Staged deployments across Dev, Staging, and Production with 1-click rollback.",
            category = "DevOps & SDLC",
            isEnabled = true
        ),
        SkillItem(
            name = "incident-rca",
            description = "Production incident post-mortems, root cause analysis, and remediation plans.",
            category = "DevOps & SDLC",
            isEnabled = true
        ),
        SkillItem(
            name = "semantic-release",
            description = "Automated Semantic Versioning (SemVer) and AI-generated release changelogs.",
            category = "DevOps & SDLC",
            isEnabled = true
        ),
        SkillItem(
            name = "git-branch-protections",
            description = "Enforce pull request approvals, required CI passes, and protected branches.",
            category = "DevOps & SDLC",
            isEnabled = true
        ),

        // ==========================================
        // 9. Security, Quality & SAIF
        // ==========================================
        SkillItem(
            name = "security-guardrails",
            description = "Destructive shell command denylist, workspace confinement, and token masking.",
            category = "Security & SAIF",
            isEnabled = true
        ),
        SkillItem(
            name = "enterprise-audit",
            description = "Structured compliance audit logging with standardized JSON export.",
            category = "Security & SAIF",
            isEnabled = true
        ),
        SkillItem(
            name = "saif-security",
            description = "Google Secure AI Framework (SAIF) threat modeling and data leakage prevention.",
            category = "Security & SAIF",
            isEnabled = true
        ),
        SkillItem(
            name = "sonarqube-quality",
            description = "Static code analysis, code coverage gating, and vulnerability scanning.",
            category = "Security & SAIF",
            isEnabled = true
        ),
        SkillItem(
            name = "automated-testing",
            description = "Exhaustive unit tests, Mockito harnesses, and boundary fuzz testing.",
            category = "Security & SAIF",
            isEnabled = true
        ),

        // ==========================================
        // 10. Bio, Science & Life Sciences
        // ==========================================
        SkillItem(
            name = "alphafold-database-fetch-and-analyze",
            description = "Retrieve and analyze AlphaFold predicted 3D structures and pLDDT confidence scores by UniProt ID.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "alphagenome-atlas-website-links",
            description = "Deep-links and locus exploration URLs for the AlphaGenome Atlas.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "alphagenome-single-variant-analysis",
            description = "Analyzes genetic variant effects on RNA-seq expression, chromatin accessibility, and transcription factors.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "alphagenome-variant-impact-score",
            description = "Scores and annotates functional impact of genetic variants using AlphaGenome Variant Impact (AVI) scores.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "chembl-database",
            description = "Query ChEMBL for bioactive molecules, drug targets, IC50/Ki values, and chemical structures.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "clinical-trials-database",
            description = "Query ClinicalTrials.gov APIv2 for trials by condition, NCT ID, drug, and eligibility criteria.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "clinvar-database",
            description = "Pathogenicity classifications (Pathogenic, Benign, VUS) and clinical evidence rationales for human genomic variants.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "dbsnp-database",
            description = "Map and search short genetic variants (SNPs, indels) in NCBI dbSNP resolving rsIDs, VCF, and HGVS.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "embl-ebi-ols",
            description = "Query biomedical ontology terms and hierarchies across 250+ ontologies in the EMBL-EBI Ontology Lookup Service.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "encode-ccres-database",
            description = "Query the ENCODE Registry of cis-Regulatory Elements (cCREs) and ChIP-seq experimental peak data.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "ensembl-database",
            description = "Resolve gene, transcript, and protein IDs, fetch sequences, and get variant effect predictions (VEP).",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "foldseek-structural-search",
            description = "3D structural searches of proteins against PDB and AlphaFold using Foldseek.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "gnomad-database",
            description = "Query Genome Aggregation Database (gnomAD) for allele frequencies and gene constraint metrics (pLI, LOEUF).",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "google-maps-platform",
            description = "Architect and implement production code using Google Maps Platform APIs (routes, places, geocoding).",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "gtex-database",
            description = "Quantitative RNA expression data and variant eQTL information from the GTEx Project across 54 tissue sites.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "human-protein-atlas-database",
            description = "Protein expression and spatial subcellular localization data from the Human Protein Atlas (HPA).",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "interpro-database",
            description = "Identify domains, families, and functional sites in proteins across InterPro, Pfam, and InterPro-N.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "jaspar-database",
            description = "Query JASPAR for Transcription Factor (TF) binding profiles, PFMs, and position weight matrices.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "literature-search-arxiv",
            description = "Search scientific papers, preprints, and publications on arXiv with abstract and PDF retrieval.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "literature-search-biorxiv",
            description = "Browse, filter, and retrieve life sciences and biomedical preprints from bioRxiv and medRxiv.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "literature-search-europepmc",
            description = "Search Europe PMC for scientific literature, citations, and open-access full-text XML.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "literature-search-openalex",
            description = "Query OpenAlex scholarly database for papers, citations, h-index, and research taxonomies.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "ncbi-sequence-fetch",
            description = "Retrieve protein and nucleotide sequences from NCBI databases using E-utilities.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "openfda-database",
            description = "Query openFDA API for adverse events, drug recalls, 510(k) clearances, and NDC labeling.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "opentargets-database",
            description = "Query Open Targets Platform for target-disease associations, drug target discovery, and tractability.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "pdb-database",
            description = "Search and download experimentally-determined 3D biomolecular structures from the Protein Data Bank (PDB).",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "predictingthepast",
            description = "Ancient text restoration, dating, attribution, and contextualization via Aeneas and Ithaca.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "protein-sequence-msa",
            description = "Multiple sequence alignment of proteins using EBI Clustal Omega.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "protein-sequence-similarity-search",
            description = "Search for homologous protein sequences using MMseqs2 or BLAST.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "pubchem-database",
            description = "Query PubChem for chemical structures, SMILES, CID properties, and bioactivity assays.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "pubmed-database",
            description = "Search PubMed for scientific literature, clinical trials, and biomedical citations.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "pymol",
            description = "Visualize, analyze, and render 3D protein structures, contacts, and binding sites using PyMOL.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "quickgo-database",
            description = "Map genes to Gene Ontology biological processes, molecular functions, and cellular components.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "reactome-database",
            description = "Query Reactome biological pathway hierarchies, reaction participants, and pathway enrichment.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "string-database",
            description = "Query STRING for protein-protein interactions (PPIs), functional enrichment, and confidence scores.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "ucsc-conservation-and-tfbs",
            description = "Evolutionary conservation scores (phyloP, phastCons) and TF binding sites from UCSC Genome Browser.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "unibind-database",
            description = "Experimentally validated direct transcription factor binding sites from the UniBind database.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "uniprot-database",
            description = "Universal protein sequence and functional annotation resource across UniProtKB, UniParc, and UniRef.",
            category = "Bio & Science",
            isEnabled = true
        ),
        SkillItem(
            name = "uv",
            description = "High-performance Python package installer and resolver toolchain manager.",
            category = "Bio & Science",
            isEnabled = true
        )
    )

    fun searchSkills(query: String, category: String? = null): List<SkillItem> {
        val q = query.trim().lowercase()
        return allDesktopSkills.filter { skill ->
            (category == null || category == "All" || skill.category == category) &&
                    (q.isEmpty() || skill.name.lowercase().contains(q) ||
                            skill.description.lowercase().contains(q) ||
                            skill.category.lowercase().contains(q))
        }
    }
}
