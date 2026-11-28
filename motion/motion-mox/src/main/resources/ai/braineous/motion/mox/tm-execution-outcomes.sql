SELECT * FROM tm_observability.aggregate('[{"$match":{"ok":{"$type":"bool"}}},{"$group":{"_id":"$ok","count":{"$sum":1}}},{"$project":{"_id":0,"ok":"$_id","count":1}}]','{}')
