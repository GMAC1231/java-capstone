#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PATIENT_EMAIL="${PATIENT_EMAIL:-jane.doe@example.com}"
PATIENT_PASSWORD="${PATIENT_PASSWORD:-passJane1}"
PATIENT_ID="${PATIENT_ID:-1}"

mkdir -p evidence-output

echo "Question 24: all doctors"
curl -sS "${BASE_URL}/doctor/all" \
  | tee evidence-output/question-24-all-doctors.json
echo
echo

echo "Patient login"
LOGIN_RESPONSE="$(
  curl -sS -X POST "${BASE_URL}/patient/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${PATIENT_EMAIL}\",\"password\":\"${PATIENT_PASSWORD}\"}"
)"

printf '%s\n' "${LOGIN_RESPONSE}" \
  | tee evidence-output/patient-login.json
echo

TOKEN="$(
  printf '%s' "${LOGIN_RESPONSE}" |
  python3 -c 'import json,sys; print(json.load(sys.stdin).get("token",""))'
)"

if [[ -z "${TOKEN}" ]]; then
  echo "ERROR: No token was returned. Check the patient email/password."
  exit 1
fi

echo "Question 25: appointments for patient ID ${PATIENT_ID}"
curl -sS \
  "${BASE_URL}/patient/appointments/${PATIENT_ID}/patient/${TOKEN}" \
  | tee evidence-output/question-25-patient-appointments.json
echo
echo

echo "Question 26: Cardiologist doctors available in AM"
curl -sS \
  "${BASE_URL}/doctor/filter?speciality=Cardiologist&time=AM" \
  | tee evidence-output/question-26-filtered-doctors.json
echo
