@ECHO OFF

curl -X GET "https://pasta.lternet.edu/package/search/eml?defType=edismax&q=subject:%28%22Landsat+Enhanced+Thematic+Mapper+image+data+for+Hubbard+Brook+LTER%22%29&fq=-scope:ecotrends&fl=id,packageid,title,author,organization,pubdate,coordinates&debug=false&start=0&rows=2&sort=score,desc"
