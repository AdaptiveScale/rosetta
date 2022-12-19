import csv
import os

from google.cloud import bigquery
from google.cloud.bigquery import Table
from google.cloud.exceptions import NotFound

client = bigquery.Client()

PROJECT_ID = "adaptivescale-178418"
DATASET = "rosetta_demo"
DATA_DIR = "data"

def read_csv_from_cloud():
    pass


def insert_rows(table_name):
    table_id = "{}.{}.{}".format(PROJECT_ID, DATASET, table_name)
    table = Table.from_string(table_id)

    try:
        data = csv.DictReader(open("{}/{}.csv".format(DATA_DIR, table_name)))
        errors = client.insert_rows_json(table, data)
        if not errors:
            print("New rows have been added in {}".format(table_name))
        else:
            print("Encountered errors while inserting rows: {}".format(errors))
    except NotFound:
        print("Table {} not found.".format(table_name))


def insert_data():
    files = os.listdir(DATA_DIR)
    for file in files:
        table_name = file.split(".")[0]
        print("Inserting data in the table {} from file {}.".format(table_name, file))
        insert_rows(table_name)


if __name__ == "__main__":
    insert_data()
