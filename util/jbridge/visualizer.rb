#! /usr/bin/ruby -w

require 'util/jbridge/common'

JavaBridge.addcp(["Visualizer.jar", "lib/prefuse.jar", "lib/forms-1.2.0.jar"
                 ].collect { |p| "Visualizer/#{p}" })

# I really do not like the following include! I would prefer to not
# pollute the main namespace, but yajb needs this include (I should
# file a bug).
include JavaBridge 

# Adapter module to java: Visualizer.jar
module Visualizer
  # Table data representation.
  #
  # Adapter class to java: Visualizer.jar
  class Table
    # the table data (two-dimensional array of numbers)
    attr_reader :data
    # array of strings representing the x-labels
    # (this is the _inner_ data of the java Visualizer)
    attr_reader :xn
    # array of strings representing the y-labels
    # (this is the _outer_ data of the java Visualizer)
    attr_reader :yn

    
    # _xn_:: array of strings representing the x-labels
    #        (this is the _inner_ data of the java Visualizer)
    # _yn_:: array of strings representing the y-labels
    #        (this is the _outer_ data of the java Visualizer)
    # _data_:: a two-dimensional array of numbers
    #          (i.e. an array containing _xn_.length arrays with
    #          each having _yn_.length numeric entries)
    # If _data_ is not given, it is initialized with zeros.
    def initialize(xn, yn, data=nil)
      @xn = xn.collect { |s| s.to_s }
      @yn = yn.collect { |s| s.to_s }
      @data = data || initdata
    end

    def initdata # :nodoc:
      Array.new(@xn.length) { Array.new(@yn.length,0) }
    end

    # :call-seq:
    # [x,y]=z
    #
    # sets value at _x_,_y_ to _z_.
    def []=(x,y,v)
      @data[x][y]=v
    end

    # :call-seq:
    # [x]
    #
    # returns the corresponding y array. This allows e.g.
    #   table[x][y] = 4.2
    def [](x)
      @data[x]
    end

    def javaobj # :nodoc:
      jnew("visualizer.ruby.FlatTable", [:t_double]+@data.flatten, @xn, @yn)
    end

    # call the java Visualizer with this table.
    def visualize
      fc = javaobj
      vm = jnew("visualizer.VisuMain")
      vm.init(fc)
    end

    def header_to_string
      s =  "xn = " + @xn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "yn = " + @yn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s
    end
    def to_string
      header_to_string << "data = " + @data.join(', ') + "\n"
    end
  end

  class Cube < Table
    attr_reader :zn
    def initialize(xn, yn, zn, data=nil)
      @zn = zn
      super(xn, yn, data)
    end

    def initdata
      Array.new(@xn.length) { Array.new(@yn.length) { Array.new(@zn.length,0)}}
    end

    def []=(x,y,z,v)
      @data[x][y][z]=v
    end

    def javaobj
      jnew("visualizer.ruby.FlatCube", [:t_double]+@data.flatten, @xn,@yn,@zn)
    end

    def header_to_string
      s = super
      s << "zn = " + @zn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s
    end
  end

  class CubeOld # :nodoc:
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
  def jb_visualize2d(xn, yn)
    Visualizer::Table.new(xn, yn, self).visualize
  end

  def jb_visualize3d(xn, yn, zn)
    Visualizer::Cube.new(xn, yn, zn, self).visualize
  end
end


