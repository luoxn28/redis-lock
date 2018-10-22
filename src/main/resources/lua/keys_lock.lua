for i, key in pairs(KEYS) do
  if (not redis.call('set', key, ARGV[1], 'ex', ARGV[2], 'nx')) then
    if (i > 1) then
      for j = 1, i-1 do
        redis.call('del', KEYS[j])
      end
    end
    return key
  end
end
return 'ok'