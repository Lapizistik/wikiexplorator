#! /usr/bin/ruby -w

require 'util/jbridge/common'

JavaBridge.addcp(["Visualizer.jar", "lib/prefuse.jar", "lib/forms-1.2.0.jar"
                 ].collect { |p| "Visualizer/#{p}" })

# I really do not like the following include! I would prefer to not
# pollute the main namespace, but yajb needs this include (I should
# file a bug).
include JavaBridge 

module Visualizer
  class Cube
    attr_reader :data
    attr_reader :xn, :yn, :zn
    def initialize(xn, yn, zn, data=nil)
      @xn = xn
      @yn = yn
      @zn = zn
      unless @data = data
        @data = Array.new(@xn.length) { 
          Array.new(@yn.length) { Array.new(@zn.length,0) } }
      end
    end
    def []=(x,y,z,v)
      @data[x][y][z]=v
    end
    def [](x)
      @data[x]
    end
    def visualize
      fc = jnew("visualizer.ruby.FlatCube",
                [:t_double] + @data.flatten, @xn, @yn, @zn)
      vm = jnew("visualizer.VisuMain")
      vm.init(fc)
    end
    def to_string
      s =  "xn = " + @xn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "yn = " + @yn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "zn = " + @zn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "data = " + @data.join(', ') + "\n"
      s
    end
  end
end


module Enumerable
  def jb_visualize3d(xn, yn, zn)
    Visualizer::Cube.new(xn, yn, zn, self).visualize
  end
end


