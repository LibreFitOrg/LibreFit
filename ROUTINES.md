# Routine library

The routine templates shipped with the app are defined in
`app/src/main/res/raw/routines.json` (validated by `validate_routines_json.py`
against `schemas/routines-schema.json`).

Categories are declared as string resources (`routine_category_*`) in
`strings.xml` and displayed in this order in the library screen
(`LIBRARY_CATEGORY_KEYS` in `LibraryScreenViewModel.kt`); empty categories
are hidden.

## Categories overview

| Category key | Label (EN) | Label (FR) | Templates |
|---|---|---|---|
| `routine_category_beginner` | Beginner | Débutant | 2 |
| `routine_category_full_body` | Full body | Corps entier | 6 |
| `routine_category_ppl` | Push / Pull / Legs | Push / Pull / Legs | 3 |
| `routine_category_strength` | Strength | Force | 2 |
| `routine_category_hypertrophy` | Hypertrophy | Hypertrophie | 4 |
| `routine_category_bodyweight` | Bodyweight & home | Poids du corps & maison | 3 |
| `routine_category_cardio` | Cardio & fat loss | Cardio & perte de poids | 5 |
| `routine_category_targeted` | Targeted | Ciblé | 3 |
| `routine_category_mobility` | Mobility & recovery | Mobilité & récupération | 2 |

**Total: 30 templates**

## Routines per category

### Beginner — Débutant (2)

- Dumbbell_Discovery — Dumbbell Discovery
- Resistance_Bands_Full_Body — Full Body with Bands

### Full body — Corps entier (6)

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

### Strength — Force (2)

- Upper_Body_Strength — Upper Body Strength
- Lower_Body_Strength — Lower Body Strength

### Hypertrophy — Hypertrophie (4)

- Chest_And_Back — Chest & Back
- Arms_And_Shoulders — Arms & Shoulders
- Upper_Body_Hypertrophy — Upper Body Hypertrophy
- Lower_Body_Hypertrophy — Lower Body Hypertrophy

### Bodyweight & home — Poids du corps & maison (3)

- Bodyweight_Upper_Home — Home Upper – Bodyweight
- Bodyweight_Legs_Home — Home Legs – Bodyweight
- Core_And_Abs — Core & Abs

### Cardio & fat loss — Cardio & perte de poids (5)

- Treadmill_Interval_Run — Treadmill Interval Run
- Twenty_Minute_HIIT — HIIT 20 Minutes
- Cardio_Burn_Machines — Cardio Burn – Machines
- Metabolic_Dumbbell_Circuit — Metabolic Dumbbell Circuit
- Walk_And_Move — Walk & Move

### Targeted — Ciblé (3)

- Wide_Back_Lats — Wide Back & Lats
- Glutes_And_Legs — Glutes & Legs
- Posture_And_Upper_Back — Posture & Upper Back

### Mobility & recovery — Mobilité & récupération (2)

- Mobility_And_Stretching — Mobility & Stretching
- Comeback_After_A_Break — Comeback After a Break
