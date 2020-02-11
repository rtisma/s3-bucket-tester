# s3-bucket-tester
CLI tool to import icdc-dcc SONG data into an intermediate SONG server while preserving the objectIds. 
This is a temporary script that should only be run by system administrators for intermediate SONG. 
Warning: this script does not preserve the ID space of intermediate SONG. It is sole purpose is to 
import data into intermediate SONG and publish it without having to copy/move object data around.

## Requirements
- java 11
- maven 3.6.0+
- GNU Make
- bash

## Building
```bash
make build
```

## Testing with Docker
Note: `docker` and `docker-compose` must be installed
```bash
make test
```

## Debugging using Docker
```bash
make clean build start-services
```
After setting debug breakpoints via the IDE, the test `testCommand` located in the `CommandTest.java` file, can be run in debug mode.


## Configuration
After building, you can create 
```bash
# Set the source song's configuration
./target/dist/bin/intermediate-song-importer config set source -p myprofile -a <accessToken> -u <sourceSongUrl>

# Set the target song's configuration
./target/dist/bin/intermediate-song-importer config set target -p myprofile -a <accessToken> -u <sourceSongUrl> \
  --db-name song --db-username postgres --db-password password --db-hostname localhost --db-port 5432
```

You can get a list of available profiles via
```bash
./target/dist/bin/intermediate-song-importer config get --list
```

And you can get the configuration for a profile via
```bash
./target/dist/bin/intermediate-song-importer config get -p myprofile
```

## Running
After completing configuration, you can run with
```bash
./target/dist/bin/intermediate-song-importer run -p myprofile -d <inputDir>
```
where `inputDir` contains files with names formatted to `<analysisId>.json`

## Real Scenario
After initializing the `argo-meta` submodule with
```bash
git submodule update --init --recursive
```

and creating the studyId `PACA-CA`, the following command can be run to execute the import
```bash
./target/dist/bin/intermediate-song-importer config set -p myprofile source -u https://song.cancercollaboratory.org -a <access token with collab.WRITE scope>
./target/dist/bin/intermediate-song-importer config set -p myprofile target -u <intermediate-song-url> -a <access token for intermediate-song> -dn <dbname> -du <username> -dq <password> -dh <hostname> -dp <port>
./target/dist/bin/intermediate-song-importer run -p myprofile -d ../../../argo-meta/icgc_song_payloads/PACA-CA
```

## Input dir rules
The `inputDir` directory, must contain json file that represent the payload to be submitted to the target song server (i.e intermediate song server). 
The filename must match the convention:

`<sourceAnalysisId>.json`

where  `sourceAnalysisId` implies an existing analysisId from the source (or reference) song server.
The contents of the payload files can contain any studyId (which will be created on the target if it does not exist) and the payloads them selves will be `submitted` to the target song server as is. It is important to note, that the files from the payload `MUST` be a subset of the files associated with `sourceAnalysisId`.


## Misc
### Nuking the intermediate-song-db
When testing or when debugging, it can be useful to refresh or nuke the intermediate-song-db. To do this run:
```bash
make refresh-intermediate-song-db
```
