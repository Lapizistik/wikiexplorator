#!/usr/bin/ruby -w

require 'util/epstopdf'

# The Gnuplot class is a kind of wrapper for the gnuplot program.
# I was aware of the +gnuplot+ GEM but needed something slightly different.
# (In the beginning I tried to stay kind of compatible but skipped this later)
# 
# On windows you may use either the win32 or the dj2 (DOS, command line) 
# gnuplot version (or if you are a cygwin user the cygwin version which works
# best). Both have some advantages/disadvantages:
# * The dos version (<tt>gnuplot.exe</tt>) is best for batch, i.e. to create 
#   nice bitmap or vector graphics, but the interactive graph display is 
#   somehow limited.
# * The windows version (<tt>wgnuplot</tt> and <tt>pgnuplot</tt>) gives a
#   nice graphical window but has known problems with larger data sets
#   (data miss by buffer overflow).
#   So to use this version set the environment variable <tt>RB_GNUPLOT</tt>
#   to <tt>"pgnuplot"</tt> (you may also specify the full path) and the
#   environment variable <tt>RB_GNUPLOT_DELAY</tt> to <tt>0.01</tt> which
#   slows down the data stream.
class Gnuplot
  include Epstopdf

  # the gnuplot command
  CMD = ENV['RB_GNUPLOT'] || 'gnuplot'

  # gnuplot pipe delay in seconds (only for pgnuplot on windows!).
  # If there are problems with hanging gnuplot processes try to set this to
  # something around 0.01
  PIPEDELAY = ((pd = ENV['RB_GNUPLOT_DELAY']) && pd.to_f)

  # gnuplot zero time
  T0 = Time.gm(2000)

  # A collection of all datasets to be plottet 
  attr_reader :datasets

  # Create a new Gnuplot object. 
  #
  # This does _not_ start a gnuplot process.
  #
  # The gnuplot process is started when calling #plot, #splot or #command!
  #
  # At this time all settings collected within this object are piped to
  # the gnuplot process (in given order).
  #
  # You may give a block to do neat things, e.g.:
  #  Gnuplot.new do |gp|
  #    gp << [2,3,1,4,5]
  #    gp << 'sin(x)'
  #    gp.add([3,2,3,2,3], :with => 'lines', :title => 'aha')
  #    gp << [1,4,3,1,4]
  #    gp.add([[2,2],[2,1],[1,1],[1,3],[3,3],[3,0]], :with => 'lines')
  #    gp.title = "example"
  #    gp.plot(:range => '[0:6]')
  #  end
  #
  #  Gnuplot.new do |gp|
  #    gp.set('hidden3d')
  #    gp.set('surface')
  #    gp.set('contour base')
  #    gp.add([[2, 2, 1, 1, 2, 2], [4, 3, 2, 1, 2, 1], [3, 3, 1, 2, 1, 1], [4, 5, 4, 5, 3, 2], [6, 7, 6, 7, 6, 7], [7, 6, 7, 6, 7, 6]],
  #           :with => 'lines', :matrix => true)
  #    gp.splot
  #  end
  #
  # Please refer to the gnuplot documentation for a very comprehensive
  # description of all parameters. Interactively availible by starting
  # gnuplot and typing 'help'.
  #
  # Online resources (partly in german): 
  # * http://www.gnuplot.info/
  # * http://userpage.fu-berlin.de/~voelker/gnuplotkurs/gnuplotkurs.html
  # * http://de.wikipedia.org/wiki/Gnuplot
  # * http://www.wikischool.de/wiki/WikiSchool:Plot
  #
  # For plotting the contents of only one enumerable see Enumerable#gp_plot().
  def initialize
    @sets = []
    @datasets = []
    yield(self) if block_given?
  end

  # returns the (last) value var was set to.
  def [] (var)
    @sets.reverse.assoc(var.to_s)
  end

  # Sets the gnuplot variable var to value.
  # If _quote_ is true, the value is quoted.
  #
  # All settings and commands will be executed in chronological
  # order when plot/splot is called.
  def set(var, value='', quote=false)
    value = "\"#{value}\"" if quote
    @sets << [var.to_s, value]
  end

  alias []= set

  # Sets the title of the plot.
  # 
  # The title is automatically quoted.
  # 
  # To set a title with parameters, use e.g.
  #  gp[:title] = '"This is it" 2,1'
  # or
  #  gp.set(:title, '"This is it" 2,1')
  def title=(text)
    @sets << ['title', "\"#{text}\""]
  end

  # Sets the terminal for the plot.
  #
  # You also have to set the output file/device.
  #
  # It is recommended to set the terminal before the output.
  def terminal=(t)
    @sets << ['terminal', "#{t}"]
  end

  # Sets the output file/device for the plot.
  #
  # Out is automatically quoted.
  #
  # Take care! You have to specify the according terminal
  # (see gnuplot documentation). It is recommended to set the terminal
  # before the output.
  def output=(out)
    @sets << ['output', "\"#{out}\""]
  end

  # Sets the xlabel of the plot.
  # 
  # The label is automatically quoted.
  # 
  # To set the xlabel with parameters, use e.g.
  #  gp[:xlabel] = '"X Axis" font "Verdana"'
  def xlabel=(text)
    @sets << ['xlabel', "\"#{text}\""]
  end

  # Sets the ylabel of the plot.
  # 
  # The label is automatically quoted.
  # 
  # To set the ylabel with parameters, use e.g.
  #  gp[:ylabel] = '"Y Axis" font "Verdana"'
  def ylabel=(text)
    @sets << ['ylabel', "\"#{text}\""]
  end

  # Sets the x2label of the plot.
  # 
  # The label is automatically quoted.
  # 
  # To set the x2label with parameters, use e.g.
  #  gp[:x2label] = '"X Axis" font "Verdana"'
  def x2label=(text)
    @sets << ['x2label', "\"#{text}\""]
  end

  # Sets the y2label of the plot.
  # 
  # The label is automatically quoted.
  # 
  # To set the y2label with parameters, use e.g.
  #  gp[:y2label] = '"Y Axis" font "Verdana"'
  def y2label=(text)
    @sets << ['y2label', "\"#{text}\""]
  end

  # Add cmd to the sequence of commands to be passed to gnuplot. E.g.
  #  gp.command('print "testing..."')  
  def command(cmd)
    @sets << cmd
  end

  # Add cmd to the sequence of commands to be passed to gnuplot and
  # call gnuplot with the whole chain.
  def command!(cmd, persist=true)
    @sets << cmd
    gnuplot(sets_to_s, persist)
  end

  # Adds _data_ to the datasets to be plotted by the next #plot or #splot 
  # command. _data_ either _is_ a Gnuplot::DataSet object or is converted 
  # to one. 
  #
  # See also #add().
  def << (data)
    data = data.gp_data unless data.kind_of?(DataSet)
    @datasets << data
    self
  end

  # Adds _data_ to the datasets to be plotted by the next #plot or #splot 
  # command. _data_ either _is_ a Gnuplot::DataSet object or is converted 
  # to one. Any _params_ are set for this dataset.
  #
  # See also #<<, Enumerable#gp_data().
  def add(data, params={})
    case data
    when DataSet
      data.update(params)
    when String
      data = Gnuplot::DataSet.new(data,params)
    else
      data = data.gp_data(params)
    end
    @datasets << data
    data
  end
  
  # Starts the gnuplot process, pipes all settings to it, including
  # those given in _params_ and calls the gnuplot +plot+ command with
  # all datasets (2d plotting).
  #
  # See #xplot for details.
  def plot(params={})
    xplot('plot', params)
  end

  # Starts the gnuplot process, pipes all settings to it, including
  # those given in _params_ and calls the gnuplot +splot+ command with
  # all datasets (3d plotting).
  #
  # See #xplot for details.
  def splot(params={})
    xplot('splot', params)
  end

  # Calls gnuplot with the appropriate +plot+ or +splot+ command
  # plotting all datasets as last command.
  #
  # You normally do not call this method directly but use #plot or
  # #splot.
  #
  # _plotcmd_ has to be <tt>"plot"</tt> or <tt>"splot"</tt>.
  #
  # _params_ is a Hash of shortcuts for the output format and filename.
  #
  # Examples:
  #  gp.plot(:ranges => '[1:10][0:5]') # with explicit ranges
  #  gp.plot(:png => 'test.png', :size => '640,400') # a nice bitmap.
  #  gp.plot(:svg => 'test.svg', :size => '640 400') # a nice SVG.
  #  gp.plot(:pdf => 'test.pdf') # a nice PDF.
  #  gp.plot(:pspdf => 'test.pdf') # Use this if your gnuplot does not support native pdf (see Epstopdf).
  #  gp.plot(:fig => 'test.fig') # a nice xfig.
  def xplot(plotcmd, params={})
    size = params[:size]

    if file = params[:pspdf]  # this needs special handling
      if size
        size = '4,4' if size == :square
        @sets << ['terminal', "postscript eps enhanced color size #{size}"]
      else
        @sets << ['terminal', "postscript eps enhanced color"]
      end

      epstopdf(file) do |tmpfile|
        @sets << ['output', "'#{tmpfile}'"]
        s = xplot_to_s(plotcmd, params[:ranges] || params[:range])
        gnuplot(s, params[:persist])
      end
      
    else # other output devices
      persist = false  # we assume output to go to file.
      if file = params[:png]
        size ||= '900,675'
        size = '675,675' if size == :square
        @sets << ['terminal', "png enhanced size #{size}"]
        @sets << ['output', "'#{file}'"]
      elsif file = params[:pdf]
        if size
          size = '4,4' if size == :square
          @sets << ['terminal', "pdf size #{size}"]
        else
          @sets << ['terminal', "pdf"]
        end
        @sets << ['output', "'#{file}'"]
      elsif file = params[:eps]  # this needs special handling
        if size
          size = '4,4' if size == :square
          @sets << ['terminal', "postscript eps enhanced color size #{size}"]
        else
          @sets << ['terminal', "postscript eps enhanced color"]
        end
        @sets << ['output', "'#{file}'"]
      elsif file = params[:svg]
        size = '675 675' if size == :square
        size ||= '900 675'
        @sets << ['terminal', "svg enhanced size #{size}"]
        @sets << ['output', "'#{file}'"]
      elsif file = params[:fig]
        @sets << ['terminal', "fig"]
        @sets << ['output', "'#{file}'"]
      else
        persist = true # output to GUI.
      end
      persist = params[:persist] if params.has_key?(:persist) # user overwrite

      s = xplot_to_s(plotcmd, params[:ranges] || params[:range])
      gnuplot(s, persist)
    end
  end

  def xplot_to_s(plotcmd, range) # :nodoc:
    s = sets_to_s
    s << "#{plotcmd} #{range} "
    s << @datasets.collect { |d| d.params_to_s }.join(', ') << "\n"
    s << @datasets.collect { |d| d.data_to_s }.compact.join
    s << "\n"
  end

  # this was a one-liner before I had to do the windows port...
  # (and I confess, it's kind of a hack)
  def gnuplot(s, persist=true) # :nodoc:
    Gnuplot.open(persist) do |io|
      if PIPEDELAY
        io.sync = true
        # special slowdown due to stupid windows pgnuplot limitations!
        s.split("\n").each do |ss| 
          io << ss << "\n"
          sleep(PIPEDELAY) 
        end
      else # the normal easy way:
        io << s
      end
    end
  end


  # computes a fit function and adds it to the plotables to be plotted
  # on #plot or #splot. If you add more than one fitting function
  # ensure that they use different function and fitting parameter names 
  # (unless you know what you are doing).
  # 
  # _params_ is a Hash of options:
  # <tt>:function => "f(x)=a*x+b"</tt>:: the function to be fitted.
  # <tt>:data => -1</tt>:: 
  #   the index of the plotable to be used for fitting. Defaults to -1,
  #   which is the last plotable added (see #add).
  # <tt>:via => "a,b"</tt>:: the names of the variables to be fitted
  # <tt>:ranges</tt>:: ranges to be used for the fitting
  # <tt>:using</tt>:: 
  #   the columns to be used (and how). Defauls to the :using parameter
  #   of the corresponding plotable.
  # <tt>:prepare</tt>:: 
  #   a String passed to gnuplot before doing the fitting.
  #   Use this to set initial values to variables, e.g. 
  #   <tt>:prepare => "a=3; b=7"</tt>
  # <tt>:FIT_LIMIT</tt>:: convergence epsilon (see gnuplot documentation)
  # <tt>:FIT_MAXITER => 100</tt>:: 
  #   max number of iterations (see gnuplot documentation)
  # <tt>:add => true</tt>:: 
  #   if _false_ the fit is done but the function is not added to the 
  #   plotables (use this if you just want to compute the parameters).
  # All other _params_ are forwarded to #add.
  #
  # Example:
  #   Gnuplot.new do |gp|
  #     gp << [1,2,3,4,4,3,3,2,1]
  #     gp.fit(:function => 'f(x) = a*x**2 + b*x + c', :via => 'a,b,c')
  #     gp.plot
  #   end
  def fit(params={})
    params = {
      :function => 'f(x)=a*x+b',
      :data => -1,
      :via => 'a,b',
      :FIT_MAXITER => 100,
      :add => true
    }.merge(params)
    if !((dataset=@datasets[d=params.delete(:data)]) && (data=dataset.data))
      raise ArgumentError.new("not a valid dataset index: #{d}")
    end
    prepare = params.delete(:prepare)
    fitlimit = params.delete(:FIT_LIMIT) || params.delete(:fit_limit)
    fitmaxiter = params.delete(:FIT_MAXITER) || params.delete(:fit_maxiter)
    fdef = params.delete(:function)
    fdef =~ /^(.*?)=/
    fkt = $1
    using = params.delete(:using) || dataset.using || '1:2'
    raise ArgumentError.new("invalid function definition: #{fdef}") unless fkt
    command(prepare) if prepare   # initialize parameters
    command("FIT_LIMIT = #{fitlimit}") if fitlimit
    command("FIT_MAXITER = #{fitmaxiter}") if fitmaxiter
    command(fdef)                 # define the function
    command("fit #{params[:ranges]} #{fkt} '-' using #{using} via #{params.delete(:via)}")
    command(dataset.data_to_s)
    add(fkt, params)           if params.delete(:add)
  end

  def sets_to_s
    @sets.collect { |set|
      if set.instance_of?(Array)
        "set #{set[0]} #{set[1]}"
      else
        set
      end
    }.join(";\n") + "\n"
  end

  # Start the +gnuplot+ process and connect IO to it.
  #
  # if a block is given, the IO is passed as parameter to the block and
  # is closed at the end of the block.
  def Gnuplot.open(persist=true, &block)
    cmd = CMD
    cmd += " -persist" if persist
    IO::popen(cmd, "w", &block)
  end

  # This class holds a String representing a function or an Enumerable
  # to be plotted.
  class DataSet
    attr_accessor :cmd, :data
    attr_accessor :title, :with, :using

    # _plotable_ may be a string (representing a gnuplot function),
    # or an Enumerable of values or Enumerables
    # representing a gnuplot dataset. 
    #
    # If any Strings are found in the data, they will be quoted for gnuplot.
    #  
    # If Time objects are found they are converted using :timefmt. If
    # timeformat is not set, they are silently converted to floats
    # (seconds from 2000-1-1 0:0, as this is the gnuplot zero
    # time). When using timeformat you have to ensure by yourself that
    # the gnuplot xdata, timefmt and other settings are done (and
    # certainly all DataSets must use the same format).
    #
    # Example for using Times in Dataset:
    #
    #   t = Time.local(2008,1)
    #   step = 60*60*24*30
    #   # we create an array of arrays of the form [[t1, v1], [t2,v2], ...]
    #   # with ti Times and vi some floats
    #   a = (1..10).collect { |i| [t+=step, i+rand ] }
    #   puts 'See our nice array:', a.inspect
    #   fmt = '%Y-%m-%d'
    #   Gnuplot.new do |gp|
    #     gp.set('xdata', 'time')
    #     gp.set('timefmt', fmt, true)
    #     gp.set('format x', '%b', true)
    #     gp.add(a, :timefmt => fmt, :with => 'lines')
    #     gp.plot
    #   end
    # 
    # Params is a Hash of parameters associated with this dataset:
    # :title, :with, :using, :axes, :matrix.
    def initialize(plotable, params={})
      if plotable.kind_of?(String) # we take this as a function!
        @cmd = plotable
        @data = nil
      else
        @cmd = '"-"'
        @data = plotable
      end
      update(params)
    end

    def update(params)
      @title   = params[:title]
      @with    = params[:with]
      @using   = params[:using]
      @axes    = params[:axes]
      @matrix  = params[:matrix]
      @timefmt = params[:timefmt]
      @using = "1:2" if @timefmt && !@using
    end
    
    def params_to_s
      s="#{@cmd} "
      s << "using #{@using} "     if @using
      s << "matrix "              if @matrix
      s << "title \"#{@title}\" " if @title
      s << "with #{@with} "       if @with
      s << "axes #{@axes} "   if @axes
      s
    end
    def data_to_s
      return nil unless @data # shortcut. Nothing to do.
      @data.collect { |a| 
        if a.respond_to?(:join)
          a.collect { |v| gp_s(v) }.join(' ')
        else
          gp_s(a)
        end
      }.join("\n") + "\ne\n"
    end
    def gp_s(v)
      case v
      when String
        '"'+v+'"'
      when Time
        @timefmt ? v.strftime(@timefmt) : (v-T0)
      else
        v
      end
    end
    def function?
      @data == nil
    end
  end
end

# GP = Gnuplot

module Enumerable

  # Plot this enumerable using gnuplot +plot+.
  #
  # Try e.g.
  #  [2,1,3,2,2].gp_plot(:ranges => '[0:5][0:4]', :with => 'lines')
  #  [0,2,1,3,2,2,4].gp_plot(:with => 'points pointsize 3 pointtype 7')
  #  [[1,1],[2,2],[4,3],[3,4]].gp_plot(:with => 'lines')
  #  [[1,1],[2,2],[4,3],[3,4]].gp_plot(:with => 'lines', :png => "test.png")
  #  "sin(x)".gp_plot
  #
  # _params_:
  # :timefmt:: format string for Time entries.
  # :timeaxis:: 
  #   String or Array of Strings giving the axis with timedata.
  #   Defaults to "x".
  #
  # For other _params_ see Gnuplot#plot and Gnuplot::DataSet.new.
  #
  def gp_plot(params={})
    Gnuplot.new do |gp|
      if fmt=params[:timefmt]
        axis = params.delete(:timeaxis) || 'x'
        axis.each { |i| gp.set("#{i}data",'time') }
        gp.set('timefmt', fmt, true)
      end
      gp << gp_data(params)
      gp.plot(params)
    end
  end

  # Plot this enumerable using gnuplot +splot+.
  #
  # Try e.g.
  #  [[1,1,2],[2,2,2],[4,3,1],[3,4,2]].gp_splot(:with => 'line palette')
  #  [[2,2,1,1],[4,3,2,1],[3,3,1,2],[4,5,4,5]].gp_splot(:matrix => true, :with => 'lines')
  #  
  # For _params_ see Gnuplot#splot and Gnuplot::DataSet#new.
  def gp_splot(params={})
    Gnuplot.new do |gp|
      gp << gp_data(params)
      gp.splot(params)
    end
  end

  # Return a new Gnuplot::DataSet for this Enumerable.
  # 
  # For _params_ see Gnuplot::DataSet#new.
  def gp_data(params={})
    Gnuplot::DataSet.new(self,params)
  end
end

