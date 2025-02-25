### Command:  sql
The sql commands allows the user to write SQL queries directly to the connected Database of his choice.
    rosetta [-c, --config CONFIG_FILE] sql [-h, --help] [-s, --source CONNECTION_NAME] [-q, --sql "Write SQL for you Schema"] [--output "Output DIRECTORY or FILE"]

Parameter | Description
--- | ---
-h, --help | Show the help message and exit.
-c, --config CONFIG_FILE | YAML config file.  If none is supplied it will use main.conf in the current directory if it exists.
-s, --source CONNECTION_NAME | The source connection is used to specify which models and connection to use.
-q --sql "SQL Query Code"  | specify the query you want to run in you connected DB.
-l --limit Response Row limit (Optional) | Limits the number of rows in the generated CSV file. If not specified, the default limit is set to 200 rows.
--no-limit (Optional) | Specifies that there should be no limit on the number of rows in the generated CSV file.



***Example*** (Query)
```
   rosetta sql -s mysql -q "select * from basic_library.authors;"
```
***CSV Output Example***
```CSV
surname,name,authorid
Howells,William Dean,1
Brown,Frederic,2
London,Jack,3
Blaisdell,Albert,4
Butler,Ellis,5
Machen,Arthur,6
Lucretius,Titus,7
Tagore,Rabindranath,8
Asimov,Isaac,9
Dickens,Charles,10
Emerson,Ralph Waldo,11
Canfield,Dorothy,12
Boccaccio,Givoanni,13
Orwell,George,14
Ovid,Publius,15
Stevenson,Robert Louis,16
Woolf,Virginia,17
Eliot,George,18
Edwards,Amelia B.,19
Dostoevsky,Fyodor,20
Dickinson,Emily,21
Ferber,Edna,22

```

