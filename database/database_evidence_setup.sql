-- Smart Clinic Management System:
-- Test data and stored procedures for Questions 19–23.
--
-- Run inside MySQL:
--   mysql -u root -p cms < database_evidence_setup.sql
--
-- The script does not delete existing records.

USE cms;

-- ------------------------------------------------------------------
-- Add three test doctors when they do not already exist.
-- ------------------------------------------------------------------
INSERT INTO doctor (name, specialty, email, password, phone)
SELECT 'Dr. Sarah Khan', 'Cardiologist',
       'sarah.khan@clinic.test', 'passSarah1', '0300000001'
WHERE NOT EXISTS (
    SELECT 1 FROM doctor
    WHERE email = 'sarah.khan@clinic.test'
);

INSERT INTO doctor (name, specialty, email, password, phone)
SELECT 'Dr. Ahmed Ali', 'Cardiologist',
       'ahmed.ali@clinic.test', 'passAhmed1', '0300000002'
WHERE NOT EXISTS (
    SELECT 1 FROM doctor
    WHERE email = 'ahmed.ali@clinic.test'
);

INSERT INTO doctor (name, specialty, email, password, phone)
SELECT 'Dr. Emily James', 'Dermatologist',
       'emily.james@clinic.test', 'passEmily1', '0300000003'
WHERE NOT EXISTS (
    SELECT 1 FROM doctor
    WHERE email = 'emily.james@clinic.test'
);

-- Add AM/PM availability records where missing.
INSERT INTO doctor_available_times (doctor_id, available_times)
SELECT d.id, '09:00 - 10:00'
FROM doctor d
WHERE d.email = 'sarah.khan@clinic.test'
  AND NOT EXISTS (
      SELECT 1
      FROM doctor_available_times dat
      WHERE dat.doctor_id = d.id
        AND dat.available_times = '09:00 - 10:00'
  );

INSERT INTO doctor_available_times (doctor_id, available_times)
SELECT d.id, '10:00 - 11:00'
FROM doctor d
WHERE d.email = 'ahmed.ali@clinic.test'
  AND NOT EXISTS (
      SELECT 1
      FROM doctor_available_times dat
      WHERE dat.doctor_id = d.id
        AND dat.available_times = '10:00 - 11:00'
  );

INSERT INTO doctor_available_times (doctor_id, available_times)
SELECT d.id, '14:00 - 15:00'
FROM doctor d
WHERE d.email = 'emily.james@clinic.test'
  AND NOT EXISTS (
      SELECT 1
      FROM doctor_available_times dat
      WHERE dat.doctor_id = d.id
        AND dat.available_times = '14:00 - 15:00'
  );

-- ------------------------------------------------------------------
-- Add five patients when they do not already exist.
-- ------------------------------------------------------------------
INSERT INTO patient (name, email, password, phone, address)
SELECT 'Jane Doe', 'jane.doe@example.com',
       'passJane1', '0311111111', 'Muscat, Oman'
WHERE NOT EXISTS (
    SELECT 1 FROM patient
    WHERE email = 'jane.doe@example.com'
);

INSERT INTO patient (name, email, password, phone, address)
SELECT 'John Smith', 'john.smith@example.com',
       'passJohn1', '0322222222', 'Muscat, Oman'
WHERE NOT EXISTS (
    SELECT 1 FROM patient
    WHERE email = 'john.smith@example.com'
);

INSERT INTO patient (name, email, password, phone, address)
SELECT 'Aisha Noor', 'aisha.noor@example.com',
       'passAisha1', '0333333333', 'Seeb, Oman'
WHERE NOT EXISTS (
    SELECT 1 FROM patient
    WHERE email = 'aisha.noor@example.com'
);

INSERT INTO patient (name, email, password, phone, address)
SELECT 'Omar Hassan', 'omar.hassan@example.com',
       'passOmar1', '0344444444', 'Bawshar, Oman'
WHERE NOT EXISTS (
    SELECT 1 FROM patient
    WHERE email = 'omar.hassan@example.com'
);

INSERT INTO patient (name, email, password, phone, address)
SELECT 'Mary George', 'mary.george@example.com',
       'passMary1', '0355555555', 'Al Khoudh, Oman'
WHERE NOT EXISTS (
    SELECT 1 FROM patient
    WHERE email = 'mary.george@example.com'
);

-- ------------------------------------------------------------------
-- Add April 2025 appointments for monthly/yearly procedure evidence.
-- ------------------------------------------------------------------
INSERT INTO appointment (
    doctor_id, patient_id, appointment_time, status
)
SELECT d.id, p.id, '2025-04-10 09:00:00', 1
FROM doctor d
JOIN patient p
  ON p.email = 'jane.doe@example.com'
WHERE d.email = 'sarah.khan@clinic.test'
  AND NOT EXISTS (
      SELECT 1 FROM appointment a
      WHERE a.doctor_id = d.id
        AND a.patient_id = p.id
        AND a.appointment_time = '2025-04-10 09:00:00'
  );

INSERT INTO appointment (
    doctor_id, patient_id, appointment_time, status
)
SELECT d.id, p.id, '2025-04-10 10:00:00', 1
FROM doctor d
JOIN patient p
  ON p.email = 'john.smith@example.com'
WHERE d.email = 'sarah.khan@clinic.test'
  AND NOT EXISTS (
      SELECT 1 FROM appointment a
      WHERE a.doctor_id = d.id
        AND a.patient_id = p.id
        AND a.appointment_time = '2025-04-10 10:00:00'
  );

INSERT INTO appointment (
    doctor_id, patient_id, appointment_time, status
)
SELECT d.id, p.id, '2025-04-11 09:00:00', 1
FROM doctor d
JOIN patient p
  ON p.email = 'aisha.noor@example.com'
WHERE d.email = 'sarah.khan@clinic.test'
  AND NOT EXISTS (
      SELECT 1 FROM appointment a
      WHERE a.doctor_id = d.id
        AND a.patient_id = p.id
        AND a.appointment_time = '2025-04-11 09:00:00'
  );

INSERT INTO appointment (
    doctor_id, patient_id, appointment_time, status
)
SELECT d.id, p.id, '2025-04-12 10:00:00', 1
FROM doctor d
JOIN patient p
  ON p.email = 'omar.hassan@example.com'
WHERE d.email = 'ahmed.ali@clinic.test'
  AND NOT EXISTS (
      SELECT 1 FROM appointment a
      WHERE a.doctor_id = d.id
        AND a.patient_id = p.id
        AND a.appointment_time = '2025-04-12 10:00:00'
  );

-- ------------------------------------------------------------------
-- Add today's appointments for the daily report.
-- ------------------------------------------------------------------
INSERT INTO appointment (
    doctor_id, patient_id, appointment_time, status
)
SELECT d.id, p.id, TIMESTAMP(CURDATE(), '09:00:00'), 0
FROM doctor d
JOIN patient p
  ON p.email = 'jane.doe@example.com'
WHERE d.email = 'sarah.khan@clinic.test'
  AND NOT EXISTS (
      SELECT 1 FROM appointment a
      WHERE a.doctor_id = d.id
        AND a.patient_id = p.id
        AND DATE(a.appointment_time) = CURDATE()
        AND TIME(a.appointment_time) = '09:00:00'
  );

INSERT INTO appointment (
    doctor_id, patient_id, appointment_time, status
)
SELECT d.id, p.id, TIMESTAMP(CURDATE(), '10:00:00'), 0
FROM doctor d
JOIN patient p
  ON p.email = 'john.smith@example.com'
WHERE d.email = 'sarah.khan@clinic.test'
  AND NOT EXISTS (
      SELECT 1 FROM appointment a
      WHERE a.doctor_id = d.id
        AND a.patient_id = p.id
        AND DATE(a.appointment_time) = CURDATE()
        AND TIME(a.appointment_time) = '10:00:00'
  );

INSERT INTO appointment (
    doctor_id, patient_id, appointment_time, status
)
SELECT d.id, p.id, TIMESTAMP(CURDATE(), '10:00:00'), 0
FROM doctor d
JOIN patient p
  ON p.email = 'aisha.noor@example.com'
WHERE d.email = 'ahmed.ali@clinic.test'
  AND NOT EXISTS (
      SELECT 1 FROM appointment a
      WHERE a.doctor_id = d.id
        AND a.patient_id = p.id
        AND DATE(a.appointment_time) = CURDATE()
        AND TIME(a.appointment_time) = '10:00:00'
  );

-- ------------------------------------------------------------------
-- Stored procedure 1: daily appointment report.
-- Required columns:
-- doctor_name, appointment_time, status, patient_name, patient_phone
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS GetDailyAppointmentReportByDoctor;

DELIMITER //

CREATE PROCEDURE GetDailyAppointmentReportByDoctor()
BEGIN
    SELECT
        d.name AS doctor_name,
        a.appointment_time AS appointment_time,
        CASE
            WHEN a.status = 0 THEN 'Scheduled'
            WHEN a.status = 1 THEN 'Completed'
            ELSE 'Unknown'
        END AS status,
        p.name AS patient_name,
        p.phone AS patient_phone
    FROM appointment a
    INNER JOIN doctor d
        ON d.id = a.doctor_id
    INNER JOIN patient p
        ON p.id = a.patient_id
    WHERE DATE(a.appointment_time) = CURDATE()
    ORDER BY d.name, a.appointment_time;
END//

-- ------------------------------------------------------------------
-- Stored procedure 2: doctor with most unique patients in a month.
-- Required output columns: doctor_id, patients_seen
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByMonth//

CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(
    IN requested_month INT,
    IN requested_year INT
)
BEGIN
    SELECT
        a.doctor_id AS doctor_id,
        COUNT(DISTINCT a.patient_id) AS patients_seen
    FROM appointment a
    WHERE MONTH(a.appointment_time) = requested_month
      AND YEAR(a.appointment_time) = requested_year
    GROUP BY a.doctor_id
    ORDER BY patients_seen DESC, doctor_id ASC
    LIMIT 1;
END//

-- ------------------------------------------------------------------
-- Stored procedure 3: doctor with most unique patients in a year.
-- Required output columns: doctor_id, patients_seen
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByYear//

CREATE PROCEDURE GetDoctorWithMostPatientsByYear(
    IN requested_year INT
)
BEGIN
    SELECT
        a.doctor_id AS doctor_id,
        COUNT(DISTINCT a.patient_id) AS patients_seen
    FROM appointment a
    WHERE YEAR(a.appointment_time) = requested_year
    GROUP BY a.doctor_id
    ORDER BY patients_seen DESC, doctor_id ASC
    LIMIT 1;
END//

DELIMITER ;

-- Verification queries for Questions 19–23:
SHOW TABLES;

SELECT *
FROM patient
ORDER BY id
LIMIT 5;

CALL GetDailyAppointmentReportByDoctor();

CALL GetDoctorWithMostPatientsByMonth(4, 2025);

CALL GetDoctorWithMostPatientsByYear(2025);
