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
    # array of strings representing the a-labels
    # (this is the _outer_ data of the java Visualizer)
    attr_reader :an
    # array of strings representing the t-labels
    # (this is the _inner_ data of the java Visualizer)
    attr_reader :tn

    
    # _an_:: array of strings representing the a-labels
    #        (this is the _outer_ data of the java Visualizer)
    # _tn_:: array of strings representing the t-labels
    #        (this is the _inner_ data of the java Visualizer)
    # _data_:: a two-dimensional array of numbers
    #          (i.e. an array containing _tn_.length arrays with
    #          each having _an_.length numeric entries)
    #          <b>Take care:</b> this is the other way around than for
    #          Visualizer::Cube. Use _data_.+transpose+ on purpose.
    # If _data_ is not given, it is initialized with zeros.
    def initialize(an, tn, data=nil)
      @tn = tn.collect { |s| s.to_s }
      @an = an.collect { |s| s.to_s }
      @data = data || initdata
    end

    def initdata # :nodoc:
      Array.new(@tn.length) { Array.new(@an.length,0) }
    end

    # :call-seq:
    # [<i>a</i>,<i>t</i>]=<i>v</i>
    #
    # sets value at _a_,_t_ to _v_.
    def []=(a,t,v)
      @data[t][a]=v
    end

    def javaobj # :nodoc:
      jnew("visualizer.ruby.FlatTable", [:t_double]+@data.flatten, @tn, @an)
    end

    # call the java Visualizer with this table.
    def visualize
      fc = javaobj
      vm = jnew("visualizer.VisuMain")
      vm.init(fc)
    end

    def header_to_string # :nodoc:
      s =  "xn = " + @tn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "yn = " + @an.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s
    end
    def to_string # :nodoc:
      header_to_string << "data = " + @data.join(', ') + "\n"
    end

    # Save plain text representation to file.
    def save(filename)
      File.open(filename,w) { |file| file << to_string }
    end
  end

  # Cube data representation.
  #
  # Adapter class to java: Visualizer.jar
  class Cube < Table
    # _bn_:: array of strings representing the b-labels
    #        (this is the _second_ _outer_ data of the java Visualizer)
    attr_reader :bn

    # _an_:: array of strings representing the a-labels
    #        (this is the _outer_ data of the java Visualizer)
    # _bn_:: array of strings representing the b-labels
    #        (this is the _second_ _outer_ data of the java Visualizer)
    # _tn_:: array of strings representing the t-labels
    #        (this is the _inner_ data of the java Visualizer)
    # _data_:: a three-dimensional array of numbers
    #          (i.e. an array containing _an_.length arrays with
    #          each containing _bn_.length arrays with _tn_.length
    #          numeric entries each).
    #
    #          <b>Take care:</b> this is the other way around than for
    #          Visualizer::Table
    # If _data_ is not given, it is initialized with zeros.
    def initialize(an, bn, tn, data=nil)
      @bn = bn
      super(an, tn, data)
    end

    def initdata # :nodoc:
      Array.new(@an.length) { Array.new(@bn.length) { Array.new(@tn.length,0)}}
    end

    # :call-seq:
    # [<i>a</i>,<i>b</i>,<i>t</i>]=<i>v</i>
    #
    # sets value at _a_,_b_,_t_ to _v_.
    def []=(a,b,t,v)
      @data[a][b][t]=v
    end

    def javaobj # :nodoc:
      jnew("visualizer.ruby.FlatCube", [:t_double]+@data.flatten, @an,@bn,@tn)
    end

    def header_to_string # :nodoc:
      s =  "xn = " + @an.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "yn = " + @bn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "zn = " + @tn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
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


class Array
  # calls the java visualizer. See Visualizer::Table.
  #
  # _an_:: array of strings representing the y-labels
  #        (this is the _outer_ data of the java Visualizer)
  # _tn_:: array of strings representing the t-labels
  #        (this is the _inner_ data of the java Visualizer)
  # The Array must contain _tn_.length arrays with
  # each having _an_.length numeric entries)
  #
  # <b>Take care:</b> The _inner_ data of the visualization is
  # the _outer_ data in the Array. Use #transpose on purpose.
  def jb_visualize2d(an, tn)
    Visualizer::Table.new(tn, an, self).visualize
  end

  # calls the java visualizer. See Visualizer::Cube.
  #
  # _an_:: array of strings representing the a-labels
  #        (this is the _outer_ data of the java Visualizer)
  # _bn_:: array of strings representing the b-labels
  #        (this is the _second_ _outer_ data of the java Visualizer)
  # _tn_:: array of strings representing the t-labels
  #        (this is the _inner_ data of the java Visualizer)
  # The Array must contain _an_.length arrays with
  # each containing _bn_.length arrays with _tn_.length
  # numeric entries each)
  def jb_visualize3d(an, bn, tn)
    Visualizer::Cube.new(xn, yn, zn, self).visualize
  end
end


