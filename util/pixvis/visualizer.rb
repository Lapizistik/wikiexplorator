#! /usr/bin/ruby -w

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
    #          (i.e. an array containing _an_.length arrays with
    #          each having _tn_.length numeric entries)
    #
    # If _data_ is not given, it is initialized with zeros.
    def initialize(an, tn, data=nil)
      @tn = tn.collect { |s| s.to_s }
      @an = an.collect { |s| s.to_s }
      @data = data || initdata
    end

    def initdata # :nodoc:
      Array.new(@tn.length) { Array.new(@an.length,0) }
    end

    # get value/sub-array.
    def [](a)
      @data[a]
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
      File.open(filename,'w') { |file| file << to_string }
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

    def header_to_string # :nodoc:
      s =  "xn = " + @an.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "yn = " + @bn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s << "zn = " + @tn.collect { |v| "\"#{v}\"" }.join(', ') + "\n"
      s
    end
    
    # sort rows and columns
    #
    # ToDo: implement me!
    def sort(options={})
      raise 'Not implemented yet!'
      
      # the following code may one time ...
      
      mode = options[:mode] || :values
      diagonal = options[:diagonal] || (@an != @bn)
      
      symmetric = (@an == @bn)
      if options.has_key?(:symmetric)
        symmetric = options[:symmetric]
      end
      
      if (@an.length != @bn.length)
        if !diagonal
          warn 'Cube not square. Setting :diagonal => true'
          diagonal = true
        end
        if symmetric
          warn 'Cube not square. Setting :symmetric => false'
          symmetric = false
        end
      end
      
    end
    
    private
    def compute_sort_values(diagonal)
      vdata = @data.collect { |o| o.collect { |i| s=0; i.each {|t| s+=t }; s }} 
      
      if !diagonal
        @an.each_index { |i| vdata[i][i] = 0 }
      end
      
      return [vdata.collect { |a| s=0; a.each { |v| s+=v }; s },
              vdata.transpose.collect { |b| s=0; b.each { |v| s+=v }; s }]
    end
  end
end
