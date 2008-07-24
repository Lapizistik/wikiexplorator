#! /usr/bin/ruby -w

require 'yajb/jbridge'

module JavaBridge
  JBRIDGE_OPTIONS = {}

  # adds _entries_ to the java classpath. _entries_ may be a String or
  # an Array of Strings. If _front_ is true, _entries_ are added at
  # front of the classpath, otherwise they are appended at the end.
  def JavaBridge.addcp(entries, front=true)
    cp = JBRIDGE_OPTIONS[:classpath].to_a || []
    if front 
      cp = entries.to_a + cp
    else
      cp = cp + entries.to_a
    end
    JBRIDGE_OPTIONS[:classpath] = cp.join(':')
  end

  module_function(:jnew)
end
