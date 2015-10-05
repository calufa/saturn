# saturn

- cd ~
- git clone git@github.com:calufa/saturn.git
```
java -cp ~/saturn/core/dist/saturn.jar Scrape 
							--domain='http://www.techcrunch.com'
							--threads=10
```

### advanced
```
java -cp ~/saturn/core/dist/saturn.jar Scrape 
              --domain='http://www.techcrunch.com'
              --threads=10
              --clearCache=true
              --downloadedURLsDirectory=
              --initListFilePath=
              --ignoreRulesFilePath
              --foundURLsHashesFilePath
              --cookies
```
