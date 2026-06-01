#!/bin/bash

DB_HOST="10.80.41.5"
DB_PORT="5432"
DB_NAME="t41n49"
DB_USER="t41n49"
DB_PASSWORD="grupo49"

SQL_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "SQL directory mounted from: $SQL_DIR"
echo "Connecting to database: $DB_NAME"
echo "Resetting public schema and running SQL scripts..."

MSYS_NO_PATHCONV=1 docker run --rm -i \
  -e PGPASSWORD="$DB_PASSWORD" \
  -v "$SQL_DIR:/sql" \
  postgres:17 \
  psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    -v ON_ERROR_STOP=1 <<SQL

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO "$DB_USER";
GRANT ALL ON SCHEMA public TO public;

SET search_path TO public;

\i /sql/01-create-model.sql
\i /sql/02-insert-data.sql
\i /sql/03-resolution.sql

SQL

if [ $? -ne 0 ]; then
    echo "ERROR: Database rebuild failed."
    exit 1
fi

echo "Database rebuild completed successfully."