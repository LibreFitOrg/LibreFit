#  SPDX-License-Identifier: GPL-3.0-or-later
#  Copyright (c) 2026. The LibreFit Contributors
#
#  LibreFit is subject to additional terms covering author attribution and trademark usage;
#  see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.

import json
import logging
import re
from PIL import Image, UnidentifiedImageError
from jsonschema import validate, ValidationError
from pathlib import Path

IMAGES_WHITELISTED_FROM_SIZE_CHECK = [
    "Arch_Hang",
    "Banded_Nordic_Curl_Negatives",
    "Bodyweight_Reverse_Hyperextension",
    "Cable_Judo_Flip",  # TODO: These must be changed eventually,
    "Eccentric_Hamstring_Slide",
    "Eccentric_Parallel_Bar_Dip",
    "Eccentric_Single_Leg_Hamstring_Slide",
    "GMB_Wrist_Prep",
    "Nordic_Curl",
    "Horizontal_Row",
    "Parallel_Bar_Dip",
    "Pike_Hanging_Leg_Raises",
    "Pistol_Squat",
    "Pseudo_Planche_Pushup",
    "Pullups",
    "RTO_Pushup",
    "RTO_PPPU",
    "Scapular_Pull-Up",
    "Shrimp_Squat",
    "Squat_Sky_Reaches",
    "Yuri_Shoulder_Band_Warmup",
    "Weighted_Horizontal_Row",
    "Weighted_Pull_Ups",
    "Wide_Row",
]

ID_WHITELISTED_FROM_REGEX = [
    "3_4_Sit-Up",
    "90_90_Hamstring",
    "Anterior_Tibialis-SMR",
    "Anti-Gravity_Press",
    "Back_Flyes_-_With_Bands",
    "Band_Assisted_Pull-Up",
    "Barbell_Ab_Rollout_-_On_Knees",
    "Barbell_Bench_Press_-_Medium_Grip",
    "Barbell_Incline_Bench_Press_-_Medium_Grip",
    "Barbell_Rollout_from_Bench",
    "Bench_Press_-_Powerlifting",
    "Bench_Press_-_With_Bands",
    "Bench_Press_with_Chains",
    "Bent-Arm_Barbell_Pullover",
    "Bent-Arm_Dumbbell_Pullover",
    "Bent-Knee_Hip_Raise",
    "Bent_Over_Low-Pulley_Side_Lateral",
    "Bent_Over_One-Arm_Long_Bar_Row",
    "Bent_Over_Two-Arm_Long_Bar_Row",
    "Bent_Over_Two-Dumbbell_Row",
    "Bent_Over_Two-Dumbbell_Row_With_Palms_In",
    "Body-Up",
    "Bottoms-Up_Clean_From_The_Hang_Position",
    "Box_Squat_with_Bands",
    "Box_Squat_with_Chains",
    "Brachialis-SMR",
    "Butt-Ups",
    "Cable_Hammer_Curls_-_Rope_Attachment",
    "Cable_Rope_Rear-Delt_Rows",
    "Calf-Machine_Shoulder_Shrug",
    "Calf_Raises_-_With_Bands",
    "Calves-SMR",
    "Catch_and_Overhead_Throw",
    "Chest_Push_from_3_point_stance",
    "Chest_Push_multiple_response",
    "Chest_Push_single_response",
    "Chest_Push_with_Run_Release",
    "Chest_Stretch_on_Stability_Ball",
    "Chin-Up",
    "Clean_and_Jerk",
    "Clean_and_Press",
    "Clean_from_Blocks",
    "Clock_Push-Up",
    "Close-Grip_Barbell_Bench_Press",
    "Close-Grip_Dumbbell_Press",
    "Close-Grip_EZ-Bar_Curl_with_Band",
    "Close-Grip_EZ-Bar_Press",
    "Close-Grip_EZ_Bar_Curl",
    "Close-Grip_Front_Lat_Pulldown",
    "Close-Grip_Push-Up_off_of_a_Dumbbell",
    "Close-Grip_Standing_Barbell_Curl",
    "Cross-Body_Crunch",
    "Cross_Over_-_With_Bands",
    "Crunch_-_Hands_Overhead",
    "Crunch_-_Legs_On_Exercise_Ball",
    "Deadlift_with_Bands",
    "Deadlift_with_Chains",
    "Decline_Close-Grip_Bench_To_Skull_Crusher",
    "Decline_Push-Up",
    "Dips_-_Chest_Version",
    "Dips_-_Triceps_Version",
    "Dumbbell_Bench_Press_with_Neutral_Grip",
    "Dumbbell_Lying_One-Arm_Rear_Lateral_Raise",
    "Dumbbell_One-Arm_Shoulder_Press",
    "Dumbbell_One-Arm_Triceps_Extension",
    "Dumbbell_One-Arm_Upright_Row",
    "Dumbbell_Seated_One-Leg_Calf_Raise",
    "Dumbbell_Tricep_Extension_-Pronated_Grip",
    "Elbow_to_Knee",
    "Exercise_Ball_Pull-In",
    "Extended_Range_One-Arm_Kettlebell_Floor_Press",
    "External_Rotation_with_Band",
    "External_Rotation_with_Cable",
    "EZ-Bar_Curl",
    "EZ-Bar_Skullcrusher",
    "Flat_Bench_Leg_Pull-In",
    "Floor_Glute-Ham_Raise",
    "Floor_Press_with_Chains",
    "Foot-SMR",
    "Forward_Drag_with_Press",
    "Frog_Sit-Ups",
    "Front_Cone_Hops_or_hurdle_hops",
    "Front_Two-Dumbbell_Raise",
    "Full_Range-Of-Motion_Lat_Pulldown",
    "Good_Morning_off_Pins",
    "Groin_and_Back_Stretch",
    "Hamstring-SMR",
    "Handstand_Push-Ups",
    "Hang_Clean_-_Below_the_Knees",
    "Hang_Snatch_-_Below_Knees",
    "Hip_Circles_prone",
    "Hip_Extension_with_Bands",
    "Hip_Flexion_with_Band",
    "Hip_Lift_with_Band",
    "Iliotibial_Tract-SMR",
    "Incline_Dumbbell_Flyes_-_With_A_Twist",
    "Incline_Push-Up",
    "Incline_Push-Up_Close-Grip",
    "Incline_Push-Up_Depth_Jump",
    "Incline_Push-Up_Medium",
    "Incline_Push-Up_Reverse_Grip",
    "Incline_Push-Up_Wide",
    "Intermediate_Hip_Flexor_and_Quad_Stretch",
    "Internal_Rotation_with_Band",
    "Inverted_Row_with_Straps",
    "Iron_Crosses_stretch",
    "Isometric_Neck_Exercise_-_Front_And_Back",
    "Isometric_Neck_Exercise_-_Sides",
    "IT_Band_and_Glute_Stretch",
    "Jackknife_Sit-Up",
    "Janda_Sit-Up",
    "Kettlebell_Figure_8",
    "Kettlebell_One-Legged_Deadlift",
    "Kettlebell_Turkish_Get-Up_Lunge_style",
    "Kettlebell_Turkish_Get-Up_Squat_style",
    "Kneeling_Single-Arm_High_Pulley_Row",
    "Landmine_180s",
    "Lateral_Raise_-_With_Bands",
    "Latissimus_Dorsi-SMR",
    "Leg-Over_Floor_Press",
    "Leg-Up_Hamstring_Stretch",
    "Leg_Pull-In",
    "Linear_3-Part_Start_Technique",
    "Lower_Back-SMR",
    "Lying_Close-Grip_Bar_Curl_On_High_Pulley",
    "Lying_Close-Grip_Barbell_Triceps_Extension_Behind_The_Head",
    "Lying_Close-Grip_Barbell_Triceps_Press_To_Chin",
    "Lying_One-Arm_Lateral_Raise",
    "Lying_T-Bar_Row",
    "Neck-SMR",
    "Oblique_Crunches_-_On_The_Floor",
    "On-Your-Back_Quad_Stretch",
    "One-Arm_Dumbbell_Row",
    "One-Arm_Flat_Bench_Dumbbell_Flye",
    "One-Arm_High-Pulley_Cable_Side_Bends",
    "One-Arm_Incline_Lateral_Raise",
    "One-Arm_Kettlebell_Clean",
    "One-Arm_Kettlebell_Clean_and_Jerk",
    "One-Arm_Kettlebell_Floor_Press",
    "One-Arm_Kettlebell_Jerk",
    "One-Arm_Kettlebell_Military_Press_To_The_Side",
    "One-Arm_Kettlebell_Para_Press",
    "One-Arm_Kettlebell_Push_Press",
    "One-Arm_Kettlebell_Row",
    "One-Arm_Kettlebell_Snatch",
    "One-Arm_Kettlebell_Split_Jerk",
    "One-Arm_Kettlebell_Split_Snatch",
    "One-Arm_Kettlebell_Swings",
    "One-Arm_Long_Bar_Row",
    "One-Arm_Medicine_Ball_Slam",
    "One-Arm_Open_Palm_Kettlebell_Clean",
    "One-Arm_Overhead_Kettlebell_Squats",
    "One-Arm_Side_Deadlift",
    "One-Arm_Side_Laterals",
    "One-Legged_Cable_Kickback",
    "One_Arm_Chin-Up",
    "Otis-Up",
    "Palms-Down_Dumbbell_Wrist_Curl_Over_A_Bench",
    "Palms-Down_Wrist_Curl_Over_A_Bench",
    "Palms-Up_Barbell_Wrist_Curl_Over_A_Bench",
    "Palms-Up_Dumbbell_Wrist_Curl_Over_A_Bench",
    "Peroneals-SMR",
    "Piriformis-SMR",
    "Plyo_Push-up",
    "Power_Clean_from_Blocks",
    "Power_Snatch_from_Blocks",
    "Press_Sit-Up",
    "Push-Up_Wide",
    "Push-Ups_-_Close_Triceps_Position",
    "Push-Ups_With_Feet_Elevated",
    "Push-Ups_With_Feet_On_An_Exercise_Ball",
    "Push_Press_-_Behind_the_Neck",
    "Push_Up_to_Side_Plank",
    "Pushups_Close_and_Wide_Hand_Positions",
    "Quadriceps-SMR",
    "Rack_Pull_with_Bands",
    "Return_Push_from_Stance",
    "Reverse_Grip_Bent-Over_Rows",
    "Rhomboids-SMR",
    "Rocky_Pull-Ups_Pulldowns",
    "Romanian_Deadlift_from_Deficit",
    "Rope_Straight-Arm_Pulldown",
    "Scapular_Pull-Up",
    "Seated_Bent-Over_One-Arm_Dumbbell_Triceps_Extension",
    "Seated_Bent-Over_Rear_Delt_Raise",
    "Seated_Bent-Over_Two-Arm_Dumbbell_Triceps_Extension",
    "Seated_Close-Grip_Concentration_Barbell_Curl",
    "Seated_Dumbbell_Palms-Down_Wrist_Curl",
    "Seated_Dumbbell_Palms-Up_Wrist_Curl",
    "Seated_Flat_Bench_Leg_Pull-In",
    "Seated_Hamstring_and_Calf_Stretch",
    "Seated_One-arm_Cable_Pulley_Rows",
    "Seated_One-Arm_Dumbbell_Palms-Down_Wrist_Curl",
    "Seated_One-Arm_Dumbbell_Palms-Up_Wrist_Curl",
    "Seated_Palm-Up_Barbell_Wrist_Curl",
    "Seated_Palms-Down_Barbell_Wrist_Curl",
    "Seated_Two-Arm_Palms-Up_Low-Pulley_Wrist_Curl",
    "See-Saw_Press_Alternating_Side_Press",
    "Shoulder_Press_-_With_Bands",
    "Side-Lying_Floor_Stretch",
    "Side_Hop-Sprint",
    "Side_Laterals_to_Front_Raise",
    "Side_to_Side_Box_Shuffle",
    "Single-Arm_Cable_Crossover",
    "Single-Arm_Linear_Jammer",
    "Single-Arm_Push-Up",
    "Single-Cone_Sprint_Drill",
    "Single-Leg_High_Box_Squat",
    "Single-Leg_Hop_Progression",
    "Single-Leg_Lateral_Hop",
    "Single-Leg_Leg_Extension",
    "Single-Leg_Stride_Jump",
    "Single_Leg_Push-off",
    "Sit-Up",
    "Sled_Drag_-_Harness",
    "Smith_Machine_Behind_the_Back_Shrug",
    "Smith_Machine_Close-Grip_Bench_Press",
    "Smith_Machine_One-Arm_Upright_Row",
    "Smith_Machine_Stiff-Legged_Deadlift",
    "Smith_Single-Leg_Split_Squat",
    "Snatch_from_Blocks",
    "Split_Squat_with_Dumbbells",
    "Squat_with_Bands",
    "Squat_with_Chains",
    "Squat_with_Plate_Movers",
    "Squats_-_With_Bands",
    "Standing_Bent-Over_One-Arm_Dumbbell_Triceps_Extension",
    "Standing_Bent-Over_Two-Arm_Dumbbell_Triceps_Extension",
    "Standing_Dumbbell_Straight-Arm_Front_Delt_Raise_Above_Head",
    "Standing_Hamstring_and_Calf_Stretch",
    "Standing_Inner-Biceps_Curl",
    "Standing_Low-Pulley_Deltoid_Raise",
    "Standing_Low-Pulley_One-Arm_Triceps_Extension",
    "Standing_One-Arm_Cable_Curl",
    "Standing_One-Arm_Dumbbell_Curl_Over_Incline_Bench",
    "Standing_One-Arm_Dumbbell_Triceps_Extension",
    "Standing_Palm-In_One-Arm_Dumbbell_Press",
    "Standing_Palms-In_Dumbbell_Press",
    "Standing_Palms-Up_Barbell_Behind_The_Back_Wrist_Curl",
    "Standing_Two-Arm_Overhead_Throw",
    "Step-up_with_Knee_Raise",
    "Stiff-Legged_Barbell_Deadlift",
    "Stiff-Legged_Dumbbell_Deadlift",
    "Straight-Arm_Dumbbell_Pullover",
    "Straight-Arm_Pulldown",
    "Straight_Raises_on_Incline_Bench",
    "Sumo_Deadlift_with_Bands",
    "Sumo_Deadlift_with_Chains",
    "Supine_One-Arm_Overhead_Throw",
    "Supine_Two-Arm_Overhead_Throw",
    "Suspended_Push-Up",
    "T-Bar_Row_with_Handle",
    "Triceps_Overhead_Extension_with_Rope",
    "Triceps_Pushdown_-_Rope_Attachment",
    "Triceps_Pushdown_-_V-Bar_Attachment",
    "Two-Arm_Dumbbell_Preacher_Curl",
    "Two-Arm_Kettlebell_Clean",
    "Two-Arm_Kettlebell_Jerk",
    "Two-Arm_Kettlebell_Military_Press",
    "Two-Arm_Kettlebell_Row",
    "Upper_Back-Leg_Grab",
    "Upright_Row_-_With_Bands",
    "V-Bar_Pulldown",
    "V-Bar_Pullup",
    "Weighted_Sit-Ups_-_With_Bands",
    "Wide-Grip_Barbell_Bench_Press",
    "Wide-Grip_Decline_Barbell_Bench_Press",
    "Wide-Grip_Decline_Barbell_Pullover",
    "Wide-Grip_Lat_Pulldown",
    "Wide-Grip_Pulldown_Behind_The_Neck",
    "Wide-Grip_Rear_Pull-Up",
    "Wide-Grip_Standing_Barbell_Curl",
    "Wrist_Rotations_with_Straight_Bar"
]


def validate_exercises(json_path: str, schema_path: str, image_folder_path: str) -> bool:
    if not Path(json_path).exists():
        logging.error(f"'{json_path}' not found.")
        return False

    if not Path(schema_path).exists():
        logging.error(f"'{schema_path}' not found.")
        return False

    # Load JSON exercises
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            exercises = json.load(f)
    except json.JSONDecodeError as e:
        logging.error(f"Invalid JSON format of {json_path}. {e}")
        return False

    # Schema validation
    try:
        with open(schema_path, 'r', encoding='utf-8') as f:
            schema = json.load(f)

        validate(instance=exercises, schema=schema)
    except json.JSONDecodeError as e:
        logging.error(f"Invalid JSON format of {schema_path}. {e}")
        return False
    except ValidationError as e:
        logging.error(f"[Path: {e.json_path}]: {e.message}")
        return False

    # Validation State
    ids = []
    # Regex strictly enforces Pascal_Snake_Case
    pascal_snake_regex = re.compile(r'^[A-Z][a-zA-Z0-9]*(_[A-Z][a-zA-Z0-9]*)*$')

    for idx, exercise in enumerate(exercises):
        ex_id = exercise.get('id', f"UNKNOWN_AT_{idx}")
        ids.append(ex_id)

        # --- GUIDELINES CHECKS ---

        # ID Formatting (Pascal_Snake_Case)
        if not pascal_snake_regex.match(ex_id) and ex_id not in ID_WHITELISTED_FROM_REGEX:
            logging.error(
                f"ID does not strictly follow Pascal_Snake_Case (no hyphens/spaces allowed). ID: {ex_id}"
            )
            return False

        # Image guidelines (webp format and exact case-sensitive folder match)
        for image_path in exercise.get('images', []):

            path = Path(f"{image_folder_path}/{image_path}")

            string_path = str(path).ljust(30)

            # Check if the file exists and is a file
            if not path.is_file():
                logging.error(
                    f"Image {string_path} does not exist."
                )
                return False

            # Check if it's less than 100 KB (100 * 1024 bytes)
            size = path.stat().st_size
            if size > 100 * 1024 and image_path.split("/")[
                0] not in IMAGES_WHITELISTED_FROM_SIZE_CHECK:
                logging.error(f"Image '{ex_id}' is too large ({size} bytes)")
                return False

            # Verify it is actually a valid WebP image (by opening it)
            try:
                with Image.open(path) as img:
                    if img.format != "WEBP":
                        raise UnidentifiedImageError
            except (UnidentifiedImageError, IOError):
                # The file is not a valid image or is corrupted
                logging.error(f"Image '{image_path}' must be .webp format.")
                return False

    # Uniqueness and ordering
    if len(ids) != len(set(ids)):
        seen = set()
        dupes = {x for x in ids if x in seen or seen.add(x)}
        logging.error(f"Found duplicate IDs: {list(dupes)}")
        return False

    # Case-insensitive alphabetical sorting comparison
    sorted_ids = sorted(ids, key=lambda x: x.lower())
    if ids != sorted_ids:
        for i, (actual, expected) in enumerate(zip(ids, sorted_ids)):
            if actual != expected:
                logging.error(
                    f"JSON is not alphabetical. First mismatch at index {i}: Expected '{expected}', found '{actual}'.")

                try:
                    # Sort efficiently using Python's built-in Timsort
                    # .lower() ensures 'A' and 'a' are treated equally during sorting
                    sorted_exercises = sorted(exercises, key=lambda x: x.get('id', '').lower())

                    output_path = 'ordered_exercises.json'

                    # Write the sorted data back to a new file with clean formatting
                    with open(output_path, 'w', encoding='utf-8') as outfile:
                        json.dump(sorted_exercises, outfile, indent=2, ensure_ascii=False)

                    logging.info(
                        f"✅ Successfully sorted {len(exercises)} exercises and saved to '{output_path}'.")
                except Exception as e:
                    logging.error(f"❌ An unexpected error occurred while ordering: {e}")

                return False

    return True


if __name__ == "__main__":
    EXERCISES_JSON_FILE = 'app/src/main/res/raw/exercises.json'
    SCHEMA_FILE = 'schemas/exercises-schema.json'
    IMAGES_FOLDER = 'app/src/main/assets'

    logging.basicConfig(level=logging.DEBUG, format='%(levelname)-8s | %(message)s')

    logging.debug(f"Exercises JSON file path : {EXERCISES_JSON_FILE}")
    logging.debug(f"Schema file path : {SCHEMA_FILE}")
    logging.debug(f"Images folder path : {IMAGES_FOLDER}")

    logging.info(f"Validating JSON...")

    verified = validate_exercises(EXERCISES_JSON_FILE, SCHEMA_FILE, IMAGES_FOLDER)

    if verified:
        print("✅ JSON file is valid.")
        exit(0)
    else:
        print("❌ JSON file is invalid.")
        exit(1)
