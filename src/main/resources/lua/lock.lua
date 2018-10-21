if (redis.call('set', KEYS[1], ARGV[1], 'ex', ARGV[2], 'nx')) then
  return true
end
return false