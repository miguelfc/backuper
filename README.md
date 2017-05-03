# backuper

Simple application to backup files across several distributed servers. Specially useful for Windows servers (for linux-only environments you shouldn't be using this utility).

## Usage

You will need to provide a configuration file, written in `yaml` format, like the following example:

```yaml
\\servername\c$\Users\miguelfc\MyVeryImportantFolder:
  destination: c:\temp\backup\MyVeryImportantFolder
  files:
  - "**/*"
  - file.json

\\anotherServer\d$\Users\nobody\Another Folder:
  destination: c:\temp\backup\Different Name
  files:
  - directory/example*
  - other*/.json
```

In the main item (e.g., `\\anotherServer\d$\Users\nobody\Another Folder`), you indicate a base folder from where to start the search. The `destination` field indicates the local folder where the files and file structure will be replicated (this dir must be empty, and it will be created if missing), and the `files` field contains a list of filesystem expressions that will be used to locate files (these use the [DirectoryScanner](https://maven.apache.org/shared/maven-shared-utils/apidocs/org/apache/maven/shared/utils/io/DirectoryScanner.html) syntax).

Then, just call the application passing the yaml file as an argument:

```sh
java -jar backuper.jar filename.yaml
```

## Notes

In order to avoid overwritting important and previous backups, the application will check that all the destination directories are empty. If one or more are not, then it will abort the operation.