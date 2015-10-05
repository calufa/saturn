# saturn

```
cd ~
```
```
git clone git@github.com:calufa/saturn.git
```
```
java -cp ~/saturn/core/dist/saturn.jar Scrape 
		--domain='http://www.techcrunch.com'
		--threads=10
```

#### Advanced
```
java -cp ~/saturn/core/dist/saturn.jar Scrape 
              --domain='http://www.techcrunch.com'
              --threads=10
              --clearCache=true
              --downloadedURLsDirectory=~/saturn/www.techcrunch.com
              --initListFilePath=~/saturn/www.techcrunch.com-initList.txt
              --ignoreRulesFilePath=~/saturn/www.techcrunch.com-ignoreRules.txt
              --foundURLsHashesFilePath=~/saturn/www.techcrunch.com.txt
              --cookies=null
```
