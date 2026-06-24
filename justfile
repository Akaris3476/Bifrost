api-create-migration name:
    ./apps/api/scripts/create-migration.sh {{name}}

acm name:
    just api-create-migration {{name}}