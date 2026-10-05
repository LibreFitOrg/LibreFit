# Routine library

The routine templates shipped with the app are declared in
`app/src/main/java/org/librefit/models/RoutineCatalog.kt`.

Templates are plain Kotlin values, not a parsed JSON file. Titles and
descriptions are referenced as string resources (`@StringRes`) so they are
resolved by the compiler and stay visible to resource shrinking (R8), and they
are translated through Weblate like any other string of the app.

## Adding a template

1. Declare `routine_<name>_title` and `routine_<name>_desc` in
   `app/src/main/res/values/strings.xml`. Do **not** add translated files by
   hand: translations only come from Weblate (see `CONTRIBUTING.md`).
2. Add a `RoutineTemplate` entry to `RoutineCatalog.all`, referencing those two
   resources and the [category](#categories-overview) it belongs to.
3. Reference exercises by their id from `res/raw/exercises.json`. The dataset is
   the single source of truth: an unknown id is skipped when the template is
   copied, so validate ids against the dataset before merging.

Nothing else has to change. The library screen, the estimated duration and the
"add to my routines" copy are all derived from `RoutineCatalog`.

## The library is a read-only catalog

Templates are static assets and are not stored in the database. The only user
state is the set of hidden template ids, kept in DataStore
(`hiddenRoutineTemplateIds`) and managed by `UserPreferencesRepository`.

Hiding a template from the library (long press on a card) therefore only removes
it from the catalog view. A template reaches the database only when the user
copies it with **Add to my routines**, which creates an independent
`WorkoutState.ROUTINE` the user fully owns and can edit.

## Categories overview

Categories are declared by the `RoutineCategory` enum and displayed in its
declaration order; empty categories are hidden.

| Category | Label (EN) | Templates |
|---|---|---|
| `BEGINNER` | Beginner | 2 |
| `FULL_BODY` | Full body | 6 |
| `PPL` | Push / Pull / Legs | 3 |
| `STRENGTH` | Strength | 2 |
| `HYPERTROPHY` | Hypertrophy | 4 |
| `BODYWEIGHT` | Bodyweight & home | 3 |
| `CARDIO` | Cardio & fat loss | 5 |
| `TARGETED` | Targeted | 3 |
| `MOBILITY` | Mobility & recovery | 2 |

**Total: 30 templates**

## Routines per category

### Beginner (2)

- Dumbbell_Discovery — Dumbbell Discovery
- Resistance_Bands_Full_Body — Full Body with Bands

### Full body (6)

- Dumbbell_Strength_Full_Body — Dumbbell Strength Full Body
- Full_Body_Beginner_Bodyweight — Full Body Beginner – At Home
- Full_Body_Beginner_Machines — Full Body Beginner – Machines
- Full_Body_Strength_Five_By_Five — Full Body Strength 5×5
- Full_Body_Strength_Five_By_Five_B — Full Body Strength 5×5 – B
- Home_No_Equipment_Full_Body — Home Full Body, No Equipment

### Push / Pull / Legs (3)

- PPL_Push — Push Day (PPL)
- PPL_Pull — Pull Day (PPL)
- PPL_Legs — Legs Day (PPL)

### Strength (2)

- Upper_Body_Strength — Upper Body Strength
- Lower_Body_Strength — Lower Body Strength

### Hypertrophy (4)

- Chest_And_Back — Chest & Back
- Arms_And_Shoulders — Arms & Shoulders
- Upper_Body_Hypertrophy — Upper Body Hypertrophy
- Lower_Body_Hypertrophy — Lower Body Hypertrophy

### Bodyweight & home (3)

- Bodyweight_Upper_Home — Home Upper – Bodyweight
- Bodyweight_Legs_Home — Home Legs – Bodyweight
- Core_And_Abs — Core & Abs

### Cardio & fat loss (5)

- Treadmill_Interval_Run — Treadmill Interval Run
- Twenty_Minute_HIIT — HIIT 20 Minutes
- Cardio_Burn_Machines — Cardio Burn – Machines
- Metabolic_Dumbbell_Circuit — Metabolic Dumbbell Circuit
- Walk_And_Move — Walk & Move

### Targeted (3)

- Wide_Back_Lats — Wide Back & Lats
- Glutes_And_Legs — Glutes & Legs
- Posture_And_Upper_Back — Posture & Upper Back

### Mobility & recovery (2)

- Mobility_And_Stretching — Mobility & Stretching
- Comeback_After_A_Break — Comeback After a Break
