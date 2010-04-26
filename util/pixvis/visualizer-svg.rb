#! /usr/bin/ruby -w

# you have to install ruby-svg <http://raa.ruby-lang.org/project/ruby-svg/>
# (either by using the provided install.rb script or by just dropping the 
# svg subdir from the archive (see directory lib) in the main mwstat directory)
require 'svg/svg'
require 'util/pixvis/visualizer'

module Visualizer
  class Table
    def to_svgfile(filename, options={}, &colorfkt)
      File.open(filename, 'w') { |f| 
        f << SVGimg.new(self, options, &colorfkt).svg }
    end
  end
#  class Cube < Table
#    def to_svgfile(filename, options={}, &colorfkt)
#      File.open(filename, 'w') { |f| 
#        f << SVGimg.new(self, options, &colorfkt).svg }
#    end
#  end

  class SVGimg
    attr_reader :svg

    # ToDo: document all the options and stuff.
    #
    #
    def initialize(dataset, options={}, &colorfkt)
      @an = dataset.an
      @bn = (dataset.respond_to?(:bn) && dataset.bn) || nil
      @tn = dataset.tn

      @amarks = options[:amarks] || options[:marks] || {}
      @bmarks = options[:bmarks] || options[:marks] || {}
      
      @dataset = dataset
      @colorfkt = colorfkt
      
      @fontfamily = options[:font_family] || 'Times New Roman'
      @fontsize = options[:font_size] || 10

      @backgroundcolor = options[:background] || '#eee'

      sep = options[:sep] 
      @xsep = options[:xsep] || sep || 0.2 * @fontsize
      @ysep = options[:ysep] || @xsep
        
      omode = options[:outer_mode] || if @bn
                                        :matrix
                                      else
                                        :lines
                                      end
      case omode
      when :matrix
#        raise 'second dimension missing' unless @bn
        create_matrix(options)
      when :lines
        create_lines(options)
      else
        raise "outer mode ':#{omode}' unknown/not implemented!"
      end

    end

    private

    def find_maxval
      if @diagonal
        @dataset.data.flatten.max
      else
        max = -1/0.0
        @an.each_index do |a|
          @bn.each_index do |b|
            if a!=b
              m = @dataset[a][b].max
              max = m if m > max
            end
          end
        end
        max
      end
    end

    def create_matrix(options)
      @diagonal = options[:diagonal] || (@an != @bn)
      maxval = options[:maxval] || find_maxval

      ioptions = { :squares => true }

      @inner_box = SVGinner.create(@tn, maxval, 
                                   ioptions.merge(options), &@colorfkt)

      (@inner_width, @inner_height) = @inner_box.size

      # without labels:
      iw = @inner_width + @xsep
      ih = @inner_height + @ysep

      @marksize = options[:marksize] || [@inner_width, @inner_height].min*0.5

      width = (iw * @bn.length) - @xsep
      height = (ih * @an.length) - @ysep

      textborder_top = options[:topsize] || 
        (@an.collect {|s| s.to_s.length}.max * @fontsize)

      textborder_left = options[:leftsize] || 
        (@bn.collect {|s| s.to_s.length}.max * @fontsize)

      textborder_top = -textborder_top - @ysep
      textborder_left = -textborder_left - @xsep

      fullwidth = width - textborder_left + @xsep
      fullheight = height - textborder_top + @ysep

      dpi = (options[:dpi] || 72).to_f # kind of contra intuitively

      inchw = fullwidth/dpi
      inchh = fullheight/dpi

      @svg = SVG.new("#{inchw}in","#{inchh}in")

      @svg.view_box = "#{textborder_left} #{textborder_top} #{fullwidth} #{fullheight}"

      background = SVG::Rect.new(textborder_left, textborder_top, 
                                 fullwidth, fullheight)
      background.style = SVG::Style.new(:fill => @backgroundcolor)
      @svg << background

      matrix = SVG::Group.new

      @an.each_with_index do |alabel, ai|
        @bn.each_with_index do |blabel, bi|
          if @diagonal || (ai != bi)
            g = @inner_box.to_svg_g(@dataset[ai][bi]) # oder bi, ai?
            g.transform = "translate(#{iw * bi},#{ih * ai})"
            matrix << g
          end
        end
      end

      @svg << matrix

      textstyle = SVG::Style.new(:text_anchor => 'start', 
                                 :font_size => @fontsize,
                                 :font_family => @fontfamily)

      @bn.each_with_index do |alabel, ai|
        text = SVG::Text.new(@ysep, iw * ai + @inner_width, alabel.to_s)
        text.style = textstyle
        text.transform = 'rotate(-90)'
        @svg << text
        add_mark(ai, alabel, @amarks, 
                 iw*ai + 0.5*@inner_width, textborder_top+@ysep+0.5*@marksize)
      end

      textstyle = SVG::Style.new(:text_anchor => 'end', 
                                 :font_size => @fontsize,
                                 :font_family => @fontfamily)

      @an.each_with_index do |blabel, bi|
        text = SVG::Text.new(-@xsep, ih * bi + @inner_height, blabel.to_s)
        text.style = textstyle
        @svg << text
        add_mark(bi, blabel, @bmarks,
                 textborder_left+@xsep+0.5*@marksize, ih*bi + 0.5*@inner_height)
      end

    end

    def create_lines(options)

      @diagonal = true
      maxval = options[:maxval] || find_maxval

      ioptions = { :inner_mode => :cols, :base_edge => :rows }

      unless options[:inner_cols] || options[:inner_rows]
        ioptions[:inner_rows] = 1
        ioptions[:pixaspectratio] = 0.1
      end


      @inner_box = SVGinner.create(@tn, maxval, 
                                   ioptions.merge(options), &@colorfkt)

      (@inner_width, @inner_height) = @inner_box.size

      # without labels:
      iw = @inner_width + @xsep
      ih = @inner_height + @ysep

      fullheight = (ih * @an.length) + @ysep

      @marksize = options[:marksize] || [@inner_width, @inner_height].min*0.5

      textborder_left = options[:leftsize] || 
        (@an.collect {|s| s.to_s.length}.max * @fontsize)
      textborder_left = -textborder_left - @xsep

      fullwidth = iw - textborder_left + @xsep

      dpi = (options[:dpi] || 72).to_f # kind of contra intuitively

      inchw = fullwidth/dpi
      inchh = fullheight/dpi

      @svg = SVG.new("#{inchw}in","#{inchh}in")
      @svg.view_box = "#{textborder_left} #{-@ysep} #{fullwidth} #{fullheight}"

      background = SVG::Rect.new(textborder_left, -@ysep, 
                                 fullwidth, fullheight)
      background.style = SVG::Style.new(:fill => @backgroundcolor)
      @svg << background

      lines = SVG::Group.new

      @an.each_with_index do |label, i|
        g = @inner_box.to_svg_g(@dataset[i])
        g.transform = "translate(0,#{ih * i})"
        lines << g
      end
      
      @svg << lines
      
      textstyle = SVG::Style.new(:text_anchor => 'end', 
                                 :font_size => @fontsize,
                                 :font_family => @fontfamily)
      
      @an.each_with_index do |label, i|
        text = SVG::Text.new(-@xsep, ih * i + @inner_height, label.to_s)
        text.style = textstyle
        @svg << text
        add_mark(i, label, @amarks,
                 textborder_left+@xsep+0.5*@marksize, ih*i + 0.5*@inner_height)
      end
    end

    def add_mark(i, label, marks, x, y)
      case marks[i] || marks[label]
      when :bullet
        c = SVG::Circle.new(x, y, 0.5*@marksize)
        c.style = SVG::Style.new(:fill => 'black')
        @svg << c
      when :circle
        c = SVG::Circle.new(x, y, 0.5*@marksize)
        c.style = SVG::Style.new(:stroke => 'black', :fill => @backgroundcolor)
        @svg << c
      when :box
        mm = 0.5*@marksize
        c = SVG::Rect.new(x-mm, y-mm, @marksize, @marksize)
        c.style = SVG::Style.new(:stroke => 'black', :fill => @backgroundcolor)
        @svg << c
      end
    end

  end
  
  private
  module SVGinner
    def SVGinner.create(tn, maxval, options, &colorfkt)
      mode = options[:inner_mode] || :rows
      case mode
      when :rows 
        Rows.new(tn, maxval, options, &colorfkt)
      when :cols 
        Cols.new(tn, maxval, options, &colorfkt)
      else
        raise "inner mode ':#{mode}' unknown/not implemented!"
      end
    end

    class Box

      attr_reader :pixsizew, :pixsizeh

      def initialize(tn, maxval, options, &colorfkt)
        @tn = tn

        @squares = options[:squares]

        @pixsizew = options[:pixsizew] || options[:pixsize]
        @pixsizeh = options[:pixsizeh] || options[:pixsize]

        @aspectratio = (options[:pixaspectratio] ||
                        if @pixsizew && @pixsizeh
                          @pixsizew.to_f / @pixsizeh
                        else
                          1
                        end).to_f
        if @pixsizew
          @pixsizeh ||= @pixsizew / @aspectratio
        elsif @pixsizeh
          @pixsizew = @pixsizeh * @aspectratio
        else
          c = @cols * @aspectratio
          bycol = if @squares
                    (c > @rows)
                  else
                    (c <= @rows)
                  end
          case options[:base_edge]
          when :rows
            bycol = false
          when :cols
            bycol = true
          end
          if bycol
            @pixsizew = 10.0 / @cols
            @pixsizeh = @pixsizew / @aspectratio
          else
            @pixsizeh = 10.0 / @rows
            @pixsizew = @pixsizeh * @aspectratio
          end
        end

        @stroke = options[:stroke] || '#000000'
        @stroke_width = options[:stroke_width] || 0

        @gamma = options[:gamma] || 1.0
        

        @colorfkt = colorfkt || 
          case options[:colorscale] || :gray
          when :temperature, :temp
            lambda { |v, max, gamma| 
            x = (v/max.to_f)**gamma
            r = (3*x*255).to_i
            g = ((3*x-1)*255).to_i
            b = ((3*x-2)*255).to_i
            r = 255 if r>255
            g = 255 if g>255
            b = 255 if b>255
            r = 0 if r<0
            g = 0 if g<0
            b = 0 if b<0
            '#%02x%02x%02x' % [r, g, b]
            }
          when :gray, :grey
            lambda { |v, max, gamma| 
            p = 255 - ((v/max.to_f)**gamma*255).to_i
            '#%02x%02x%02x' % [p, p, p]
            }
          else
            raise "unknown colorscale #{options[:colorscale].inspect}!"
          end

        @maxval = maxval

        @inner_font = options[:inner_font]  # dont use this ;-)
      end

      def size
        if @squares
          [[@cols*@pixsizew, @rows*@pixsizeh].max] * 2
        else
          [@cols*@pixsizew, @rows*@pixsizeh]
        end
      end

      def to_svg_g(data)
        g = SVG::Group.new
        g.style = SVG::Style.new(:stroke_width => 0)

        if @inner_font # dont use this ;-)
          whitestyle = SVG::Style.new(:text_anchor => 'middle', 
                                      :font_size => @inner_font,
                                      :font_family => @fontfamily,
                                      :fill => '#fff')
          blackstyle = SVG::Style.new(:text_anchor => 'middle', 
                                      :font_size => @inner_font,
                                      :font_family => @fontfamily,
                                      :fill => '#000')
          dx = 0.5 * @pixsizew
          dy = 0.5 * (@pixsizeh + @inner_font)
        end

        data.each_with_index do |v, i|
          (x, y) = xy(i)
          x *= @pixsizew
          y *= @pixsizeh
          pixel = SVG::Rect.new(x, y, @pixsizew, @pixsizeh)
          color = @colorfkt.call(v, @maxval, @gamma)
          pixel.style = SVG::Style.new(:fill => color,
                                       :stroke => @stroke,
                                       :stroke_width => @stroke_width)
          g << pixel
          if @inner_font # dont use this ;-)
            
            text = SVG::Text.new(x+dx, y+dy, v.to_s)
            if color[1..1] < '5'
              text.style = whitestyle
            else
              text.style = blackstyle
            end
            g << text
          end
        end
        return g
      end
    end

    class Rows < Box
      def initialize(tn, maxval, options, &colorfkt)
        l = tn.length
        @cols = options[:inner_cols]
        @rows = options[:inner_rows]

        @cols ||= if @rows 
                    (l.to_f / @rows).ceil 
                  else
                    Math.sqrt(l).ceil
                  end
        @rows ||= (l.to_f / @cols).ceil

        super(tn, maxval, options, &colorfkt)
      end

      def xy(i)
        i.divmod(@cols).reverse
      end
    end
    
    class Cols < Box
      def initialize(tn, maxval, options, &colorfkt)
        l = tn.length
        @cols = options[:inner_cols]
        @rows = options[:inner_rows]
        
        @rows ||= if @cols
                    (l.to_f / @cols).ceil 
                  else
                    Math.sqrt(l).ceil
                  end
        @cols ||= (l.to_f / @rows).ceil
        
        super(tn, maxval, options, &colorfkt)
      end
      
      def xy(i)
        i.divmod(@rows)
      end
    end
  end
end


#  svg = SVG.new('4in', '4in', '0 0 400 400')
#  g = SVG::Group.new { self.transform = "translate(10,100)"; self.style = SVG::Style.new(:fill => 'none', :stroke => '#000', :stroke_width => 1, :stroke_opacity => 1.0)}
#  g << SVG::Line.new(0,0,100,100)
#  svg << g
#  svg << SVG::Line.new(0,0,100,100)
#  File.open('/tmp/t.svg', 'w'){|f| f << svg.to_s}
