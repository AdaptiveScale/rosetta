## Explore and query your data using AI-driven capabilities

### Command:  query
The query command allows you to use natural language commands to query your databases, transforming these commands into SQL SELECT statements. By leveraging the capabilities of AI and LLMs, specifically OpenAI models, it interprets user queries and generates the corresponding SQL queries. For effective use of this command, users need to provide their OpenAI API Key and specify the OpenAI model to be utilized. The output will be written to a CSV file. The max number of rows that will be returned is 200. You can overwrite this value, or remove completely the limit.  The default openai model that is used is gpt-3.5-turbo.

    rosetta [-c, --config CONFIG_FILE] query [-h, --help] [-s, --source CONNECTION_NAME] [-q, --query "Natural language QUERY"] [--output "Output DIRECTORY or FILE"]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.
-q --query "Natural language QUERY"  | pecifies the natural language query to be transformed into an SQL SELECT statement.
-l --limit Response Row limit (Optional) | Limits the number of rows in the generated CSV file. If not specified, the default limit is set to 200 rows.
--no-limit (Optional) | Specifies that there should be no limit on the number of rows in the generated CSV file.


**Example** (Setting the key and model) :

(Config file)
```
openai_api_key: "sk-abcdefghijklmno1234567890"
openai_model: "gpt-4"
connections:
  - name: mysql
    databaseName: sakila
    schemaName:
    dbType: mysql
    url: jdbc:mysql://root:sakila@localhost:3306/sakila
    userName: root
    password: sakila
  - name: pg
    databaseName: postgres
    schemaName: public
    dbType: postgres
    url: jdbc:postgresql://localhost:5432/postgres?user=postgres&password=sakila
    userName: postgres
    password: sakila
```

***Example*** (Query)
```
   rosetta query -s mysql -q "Show me the top 10 customers by revenue."
```
***CSV Output Example***
```CSV
customer_name,total_revenue,location,email
John Doe,50000,New York,johndoe@example.com
Jane Smith,45000,Los Angeles,janesmith@example.com
David Johnson,40000,Chicago,davidjohnson@example.com
Emily Brown,35000,San Francisco,emilybrown@example.com
Michael Lee,30000,Miami,michaellee@example.com
Sarah Taylor,25000,Seattle,sarahtaylor@example.com
Robert Clark,20000,Boston,robertclark@example.com
Lisa Martinez,15000,Denver,lisamartinez@example.com
Christopher Anderson,10000,Austin,christopheranderson@example.com
Amanda Wilson,5000,Atlanta,amandawilson@example.com

```
**Note:**  When giving a request that will not generate a SELECT statement the query will be generated but will not be executed rather be given to the user to execute on their own.

