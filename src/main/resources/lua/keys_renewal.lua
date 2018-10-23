if (redis.call('get', KEYS[1]) == ARGV[1]) then
	for i, key in pairs(KEYS) do
		redis.call('expire', key, ARGV[2])
	end
	return true
end
return false