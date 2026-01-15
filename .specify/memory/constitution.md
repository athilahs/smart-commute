<!--
Sync Impact Report:
Version: 1.0.0 → 1.1.0 (New Development Principle Added)
Modified Principles: None
Added Sections:
  - Core Principles VIII: Proactive Documentation Access
Removed Sections: None
Templates Status:
  ✅ .specify/templates/spec-template.md - No changes needed (documentation principle doesn't affect spec structure)
  ✅ .specify/templates/plan-template.md - No changes needed (developers can use Context7 during planning without template changes)
  ✅ .specify/templates/tasks-template.md - No changes needed (task structure remains the same)
  ✅ .specify/templates/agent-file-template.md - Reviewed, no changes needed
  ✅ .specify/templates/checklist-template.md - Reviewed, no changes needed
Follow-up TODOs: None
-->

# SmartCommute Constitution

## Core Principles

### I. User-First Design

Every feature MUST start with documented user scenarios and acceptance criteria before any implementation begins. Features without clear user value or testable acceptance scenarios SHALL NOT be developed. User stories MUST be independently deliverable as incremental value.

**Rationale**: Ensures all development effort directly serves user needs and prevents over-engineering. Independent user stories enable parallel development and incremental delivery of value.

### II. Specification-Driven Development

All features MUST have a complete specification document (spec.md) approved before design work begins. Specifications MUST include user scenarios, functional requirements, success criteria, and edge cases. Changes to specifications during implementation MUST be documented and approved.

**Rationale**: Reduces rework, ensures alignment between stakeholders, and creates a single source of truth for what the feature should accomplish.

### III. No Automated Testing (NON-NEGOTIABLE)

Automated tests (unit tests, integration tests, contract tests, UI tests) MUST NOT be created for this project. Task lists MUST NOT include test tasks. Implementation plans MUST NOT allocate time or structure for test code. Manual verification and user acceptance are the validation methods.

**Rationale**: Project prioritizes rapid feature delivery and manual validation over automated test infrastructure. Resources are focused exclusively on production feature implementation.

### IV. Progressive Planning

Features MUST follow a three-phase approach: (1) Specification defining WHAT and WHY, (2) Implementation Plan defining HOW and WHEN, (3) Tasks defining executable steps. Each phase MUST be approved before proceeding to the next. Design documents MUST be created as needed (data models, API contracts, architecture decisions).

**Rationale**: Separates concerns, enables early validation of approach, and prevents implementation of misunderstood requirements.

### V. Independent User Stories

User stories MUST be prioritized (P1, P2, P3...) and structured so each can be independently implemented and deployed. A story marked P1 MUST deliver viable MVP functionality. Stories SHOULD minimize dependencies on other stories.

**Rationale**: Enables incremental delivery, parallel development, early user feedback, and the ability to stop development at any checkpoint with working functionality.

### VI. Simplicity and YAGNI

Implement only what is specified. Do NOT add abstractions, patterns, or features for hypothetical future needs. Complex solutions MUST be justified in the Complexity Tracking section of plan.md with explanation of why simpler alternatives were insufficient.

**Rationale**: Reduces cognitive load, maintenance burden, and time-to-delivery. Premature optimization and over-engineering are costly and often wrong.

### VII. Explicit Over Implicit

Requirements MUST be explicitly stated. Use "NEEDS CLARIFICATION" markers for ambiguities. File paths, entity names, and technical decisions MUST be concrete in planning documents—no placeholders in final deliverables. When uncertain, ask rather than assume.

**Rationale**: Prevents miscommunication, wasted effort on wrong assumptions, and enables objective verification of completion.

### VIII. Proactive Documentation Access

When library documentation, API references, code generation examples, setup instructions, or configuration steps are needed, Context7 MCP MUST be used proactively without requiring explicit user requests. This applies during specification, planning, implementation, and troubleshooting phases.

**Rationale**: Reduces friction in the development workflow by ensuring developers have immediate access to accurate, up-to-date documentation. Prevents delays and errors caused by searching for or guessing at API usage patterns. Supports rapid implementation by providing authoritative references during code generation.

## Android Platform Standards

### Architecture Requirements

- **MVVM Pattern REQUIRED**: All features MUST follow Model-View-ViewModel architecture
- **Single Activity**: Apps SHOULD use single Activity with Navigation Component for multi-screen flows
- **Repository Pattern**: Data access MUST be abstracted through Repository classes
- **Dependency Injection**: Use Hilt for dependency injection throughout the app
- **Reactive Streams**: Use Kotlin Flow for observable data patterns

**Rationale**: Ensures maintainable, testable code structure following Android best practices and Google's recommended architecture.

### UI/UX Standards

- **Material Design 3**: All UI components MUST follow Material Design 3 guidelines
- **Jetpack Compose**: New UI MUST be implemented using Jetpack Compose (unless legacy constraints apply)
- **Dark Mode**: All screens MUST support system dark/light theme
- **Accessibility**: Components MUST include content descriptions and support TalkBack
- **Localization**: String resources MUST use string.xml, no hardcoded text

**Rationale**: Provides consistent, accessible, and professional user experience following Android platform conventions.

### Data & State Management

- **ViewModels**: UI state MUST be managed in ViewModels, never in Activities/Fragments/Composables
- **StateFlow/LiveData**: Expose UI state as StateFlow
- **Room Database**: Local persistence MUST use Room for structured data
- **DataStore**: User preferences MUST use DataStore (not SharedPreferences)
- **Work Manager**: Background tasks MUST use WorkManager for guaranteed execution
- **Offline-First**: Apps SHOULD work offline with cached data when possible

**Rationale**: Ensures robust state management, data persistence, and resilience across process death and configuration changes.

### Performance Standards

- **Lazy Loading**: Lists MUST use LazyColumn/LazyRow or RecyclerView with pagination
- **Image Loading**: Use Glide for efficient image loading and caching
- **Main Thread**: Network calls and heavy computation MUST NOT run on main thread
- **Coroutines**: Use Kotlin Coroutines for asynchronous operations
- **Startup Time**: Cold start MUST be optimized (avoid initialization in Application class)
- **Memory**: Monitor memory usage and prevent leaks (no Activity/Context references in ViewModels)

**Rationale**: Provides smooth, responsive user experience and prevents ANR (Application Not Responding) errors.

### Security Standards

- **Network Security**: Use HTTPS only
- **Data Encryption**: Sensitive data MUST be encrypted (use EncryptedSharedPreferences, Keystore)
- **ProGuard/R8**: Release builds MUST use code obfuscation and optimization
- **Permissions**: Request minimum necessary permissions, use runtime permission checks
- **Authentication**: Use AccountManager or secure token storage for auth credentials
- **API Keys**: Never hardcode API keys, use BuildConfig or secure key management

**Rationale**: Protects user data and prevents common Android security vulnerabilities.

### Code Organization

- **Package Structure**: Organize by feature, not by layer
  ```
  com.smartcommute/
  ├── feature/
  │   ├── authentication/
  │   ├── commute/
  │   └── profile/
  ├── data/
  │   ├── repository/
  │   └── local/
  └── core/
      ├── navigation/
      └── util/
  ```
- **Resource Naming**: Follow Android resource naming conventions
- **Kotlin Best Practices**: Use data classes, sealed classes, extension functions appropriately

**Rationale**: Improves code navigation, enables parallel development, and supports modular architecture.

### Build & Configuration

- **Gradle Version Catalogs**: Manage dependencies using version catalogs
- **Build Variants**: Define debug, staging, production build variants
- **Build Types**: Configure different signing configs and API endpoints per variant
- **Min SDK**: MUST be explicitly documented in Technical Context (plan.md)
- **Target SDK**: SHOULD target latest stable Android SDK
- **Kotlin Version**: Use latest stable Kotlin version

**Rationale**: Enables consistent dependency management, environment-specific builds, and modern Android development practices.

## Quality Standards

### Documentation Requirements

- All features MUST have specification documents with user scenarios and acceptance criteria
- Implementation plans MUST include Android-specific technical context (min/target SDK, dependencies, architecture patterns)
- Task lists MUST have explicit file paths and story mappings
- Edge cases and Android lifecycle scenarios MUST be documented before implementation
- Changes to specifications MUST be documented with rationale
- NO test plans or test documentation SHALL be created

### Code Quality

- Code MUST be self-explanatory; comments only for non-obvious business logic
- Functions and classes MUST have single, clear responsibilities
- ViewModels MUST NOT hold Activity/Fragment/Context references
- Error handling MUST be explicit at system boundaries (user input, network calls)
- Security vulnerabilities (OWASP Mobile Top 10) MUST be actively prevented
- Performance requirements from specifications MUST be met and verified

### Android Lifecycle Compliance

- Handle configuration changes properly (rotation, multi-window, process death)
- Save and restore UI state in ViewModels or SavedStateHandle
- Properly manage Activity/Fragment lifecycle with lifecycle-aware components
- Cancel coroutines/jobs when lifecycle is destroyed
- Use lifecycle observers for platform integrations (location, sensors)

## Development Workflow

### Feature Development Flow

1. **Specification** (`/speckit.specify`): Create spec.md with user stories, requirements, success criteria
2. **Clarification** (`/speckit.clarify`): Resolve ambiguities with targeted questions
3. **Planning** (`/speckit.plan`): Create implementation plan with Android-specific technical design
4. **Task Generation** (`/speckit.tasks`): Generate actionable, prioritized task list (NO test tasks)
5. **Implementation** (`/speckit.implement`): Execute tasks in priority order
6. **Analysis** (`/speckit.analyze`): Verify consistency across artifacts

### Constitution Compliance

- Every implementation plan MUST include a Constitution Check section
- Violations of principles MUST be explicitly justified in Complexity Tracking
- Unjustified complexity additions MUST be rejected in code review
- Specification quality MUST be verified before approving plan phase
- Android best practices MUST be verified during implementation
- Any test-related tasks MUST be rejected immediately
- Context7 MCP MUST be used proactively for documentation needs

### Change Management

- Specification changes during implementation MUST trigger plan review
- Breaking changes to APIs or data models MUST be documented in plan.md
- User story priority changes MUST be reflected in tasks.md ordering
- Constitution amendments follow semantic versioning (see Governance section)
- Android SDK version changes MUST be documented and impact assessed

## Governance

### Amendment Procedure

1. Constitution changes MUST be proposed with clear rationale
2. All affected templates (spec, plan, tasks, commands) MUST be updated consistently
3. Version bump follows semantic versioning:
   - **MAJOR**: Principle removal or incompatible governance changes
   - **MINOR**: New principles added or material expansions
   - **PATCH**: Clarifications, wording improvements, typo fixes
4. Sync Impact Report MUST document all changes and affected templates

### Versioning Policy

Constitution follows semantic versioning: MAJOR.MINOR.PATCH
- Breaking changes to governance require team approval
- New principles can be added by project leads with documentation
- Clarifications can be made by any contributor via pull request

### Compliance Review

- Constitution compliance MUST be verified before merging implementation plans
- Code reviews MUST verify adherence to specified requirements
- Unjustified complexity MUST be challenged and simplified
- Specification quality MUST be maintained throughout feature lifecycle
- Android platform standards MUST be enforced during code review
- Test-related additions MUST be immediately flagged and removed

### Authority

This constitution supersedes all other development practices and guidelines. In conflicts between this constitution and other documents, the constitution takes precedence. Practices not covered by this constitution default to team discretion with preference for simplicity.

**Version**: 1.1.0 | **Ratified**: 2025-12-24 | **Last Amended**: 2026-01-15
