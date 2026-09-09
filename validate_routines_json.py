#  SPDX-License-Identifier: GPL-3.0-or-later
#  Copyright (c) 2026. The LibreFit Contributors
#
#  LibreFit is subject to additional terms covering author attribution and trademark usage;
#  see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.

import json
import logging
import re
from jsonschema import validate, ValidationError
from pathlib import Path

PASCAL_SNAKE_REGEX = re.compile(r'^[A-Z][a-zA-Z0-9]*(_[A-Z][a-zA-Z0-9]*)*$')

STRING_KEY_REGEX = re.compile(r'^routine_[a-z0-9_]+$')


def validate_routines(routines_path: str, schema_path: str, exercises_json_path: str,
                      strings_xml_path: str) -> bool:
    if not Path(routines_path).exists():
        logging.error(f"'{routines_path}' not found.")
        return False

    if not Path(schema_path).exists():
        logging.error(f"'{schema_path}' not found.")
        return False

    # Load routines JSON
    try:
        with open(routines_path, 'r', encoding='utf-8') as f:
            routines = json.load(f)
    except json.JSONDecodeError as e:
        logging.error(f"Invalid JSON format of {routines_path}. {e}")
        return False

    # Schema validation
    try:
        with open(schema_path, 'r', encoding='utf-8') as f:
            schema = json.load(f)

        validate(instance=routines, schema=schema)
    except json.JSONDecodeError as e:
        logging.error(f"Invalid JSON format of {schema_path}. {e}")
        return False
    except ValidationError as e:
        logging.error(f"[Path: {e.json_path}]: {e.message}")
        return False

    # Load exercise IDs from the exercises dataset
    try:
        with open(exercises_json_path, 'r', encoding='utf-8') as f:
            exercise_ids = {e['id'] for e in json.load(f)}
    except (json.JSONDecodeError, KeyError) as e:
        logging.error(f"Cannot read exercise IDs from {exercises_json_path}. {e}")
        return False

    # Load string resources to check that every titleKey/descriptionKey/categoryKey exists
    try:
        strings_content = Path(strings_xml_path).read_text(encoding='utf-8')
        defined_strings = set(re.findall(r'<string name="([^"]+)"', strings_content))
    except OSError as e:
        logging.error(f"Cannot read {strings_xml_path}. {e}")
        return False

    ids = []

    for idx, routine in enumerate(routines):
        routine_id = routine.get('id', f"UNKNOWN_AT_{idx}")
        ids.append(routine_id)

        # ID Formatting (Pascal_Snake_Case)
        if not PASCAL_SNAKE_REGEX.match(routine_id):
            logging.error(
                f"Routine ID does not follow Pascal_Snake_Case. ID: {routine_id}"
            )
            return False

        # String keys must be declared in strings.xml and follow the naming convention
        for key_field in ('titleKey', 'descriptionKey', 'categoryKey'):
            key = routine.get(key_field, '')
            if not STRING_KEY_REGEX.match(key):
                logging.error(f"'{key_field}' of '{routine_id}' must match routine_<name>: '{key}'")
                return False
            if key not in defined_strings:
                logging.error(f"String '{key}' (used by '{routine_id}') is missing in strings.xml")
                return False

        # Exercise references and per-mode consistency
        for jdx, template_exercise in enumerate(routine.get('exercises', [])):
            exercise_dc_id = template_exercise.get('idExerciseDC', '')
            if exercise_dc_id not in exercise_ids:
                logging.error(
                    f"Exercise '{exercise_dc_id}' at index {jdx} of '{routine_id}' "
                    f"is not present in exercises.json"
                )
                return False

            set_mode = template_exercise.get('setMode', '')
            has_reps = 'reps' in template_exercise
            has_elapsed_time = 'elapsedTime' in template_exercise

            if set_mode == 'DURATION' and not has_elapsed_time:
                logging.error(
                    f"Exercise '{exercise_dc_id}' of '{routine_id}' uses DURATION "
                    f"but has no 'elapsedTime'"
                )
                return False

            if set_mode != 'DURATION' and not has_reps:
                logging.error(
                    f"Exercise '{exercise_dc_id}' of '{routine_id}' does not use DURATION "
                    f"but has no 'reps'"
                )
                return False

    # Uniqueness and ordering
    if len(ids) != len(set(ids)):
        seen = set()
        dupes = {x for x in ids if x in seen or seen.add(x)}
        logging.error(f"Found duplicate routine IDs: {list(dupes)}")
        return False

    # Case-insensitive alphabetical sorting comparison
    sorted_ids = sorted(ids, key=lambda x: x.lower())
    if ids != sorted_ids:
        for i, (actual, expected) in enumerate(zip(ids, sorted_ids)):
            if actual != expected:
                logging.error(
                    f"JSON is not alphabetical. First mismatch at index {i}: "
                    f"Expected '{expected}', found '{actual}'."
                )
                return False

    return True


if __name__ == "__main__":
    ROUTINES_JSON_FILE = 'app/src/main/res/raw/routines.json'
    SCHEMA_FILE = 'schemas/routines-schema.json'
    EXERCISES_JSON_FILE = 'app/src/main/res/raw/exercises.json'
    STRINGS_XML_FILE = 'app/src/main/res/values/strings.xml'

    logging.basicConfig(level=logging.DEBUG, format='%(levelname)-8s | %(message)s')

    logging.debug(f"Routines JSON file path : {ROUTINES_JSON_FILE}")
    logging.debug(f"Schema file path : {SCHEMA_FILE}")
    logging.debug(f"Exercises JSON file path : {EXERCISES_JSON_FILE}")
    logging.debug(f"Strings XML file path : {STRINGS_XML_FILE}")

    logging.info("Validating routines JSON...")

    verified = validate_routines(
        ROUTINES_JSON_FILE, SCHEMA_FILE, EXERCISES_JSON_FILE, STRINGS_XML_FILE
    )

    if verified:
        print("✅ Routines JSON file is valid.")
        exit(0)
    else:
        print("❌ Routines JSON file is invalid.")
        exit(1)
