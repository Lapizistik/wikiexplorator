#!/usr/bin/ruby -w

require 'test/unit'
require 'pp'

$:.unshift File.join(File.dirname(__FILE__), '..') # here is the code

require 'util/pixvis/visualizer-svg'

class TestVisualizer < Test::Unit::TestCase
  def setup

    @cube = build_cube(6,5,inner=10) { |r,c,i| 
       if c.odd?
         r.odd? ? (i/2.0/inner) : (0.5+i/2.0/inner)
       else
         r.odd? ? (1-i/2.0/inner) : (0.5-i/2.0/inner)
       end
    }

    @cube1 = build_cube(6,5,10) { |r,c,i| 
      if (c==5) && (r==1)
        i
      else
        4
      end
    }

    @lines = build_lines(5,30) { |r,i|
      if r.odd?
        5
      else
        i
      end
    }

  end

  def build_cube(cols, rows, inner)
    col_names = (1..cols).collect { |i| "C#{i}" }
    row_names = (1..rows).collect { |i| "R#{i}" }
    inner_names = (1..inner).collect { |i| "I{i}" }

    data = (1..rows).collect { |r|
      (1..cols).collect { |c|
        (1..inner).collect { |i| yield(r,c,i) }
      }
    }
    Visualizer::Cube.new(row_names, col_names, inner_names, data)
  end

  def build_lines(rows, inner)
    row_names = (1..rows).collect { |i| "R#{i}" }
    inner_names = (1..inner).collect { |i| "I{i}" }
    data = (1..rows).collect { |r|
      (1..inner).collect { |i| yield(r,i) }
    }
    Visualizer::Table.new(row_names, inner_names, data)
  end

  # hm, how to test an SVG string/file? ask nokogiri for help?
  def test_cube
    #    @cube.to_svgfile('/tmp/a.svg') { '#444' }
    #    @cube.to_svgfile('/tmp/a.svg')
    #    @cube.to_svgfile('/tmp/a.svg', :gamma => 4)
    #    Visualizer::SVGimg.new(@cube).svg

    # did we build the cube right?
    assert_equal(@cube1.data[0][0][0],4)
    assert_equal(@cube1.data[0][4][9],10)
    assert_equal(@cube1.data[5],nil)
    assert_equal(@cube1.data[4][6],nil)
    assert_equal(@cube1.data[4][5][10],nil)
    assert_equal(@cube1.data.flatten.length,5*6*10)

    # now test the svg?
#    @cube1.to_svgfile('/tmp/a.svg', :pixsizew => 2, :inner_cols => 1)
   @cube1.to_svgfile('/tmp/a.svg', :inner_mode => :cols, 
                      :pixaspectratio => 0.25, :inner_rows => 1,
                     :marks => {0 => :bullet}
                     )
  end

  def test_lines
   @lines.to_svgfile('/tmp/b.svg', :colorscale => :temp, :background => '#def',
                     :marks => {'R2' => :bullet, 'R4' => :box})
#   @lines.to_svgfile('/tmp/b.svg', :inner_rows => 4)
  end
end

