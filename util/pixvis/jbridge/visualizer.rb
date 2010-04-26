#! /usr/bin/ruby -w

require 'util/pixvis/jbridge/common'
require 'util/pixvis/visualizer'

JavaBridge.addcp(["Visualizer.jar", "lib/prefuse.jar", "lib/forms-1.2.0.jar"
                 ].collect { |p| "Visualizer/#{p}" })

# I really do not like the following include! I would prefer to not
# pollute the main namespace, but yajb needs this include (I should
# file a bug).
include JavaBridge 

module Visualizer
  class Table
    def javaobj # :nodoc:
      jnew("visualizer.ruby.FlatTable", [:t_double]+@data.flatten, @tn, @an)
    end

    # call the java Visualizer with this table.
    def visualize
      fc = javaobj
      vm = jnew("visualizer.VisuMain")
      vm.init(fc)
    end
  end

  class Cube < Table
    def javaobj # :nodoc:
      jnew("visualizer.ruby.FlatCube", [:t_double]+@data.flatten, @an,@bn,@tn)
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
  # The Array must contain _an_.length arrays with
  # each having _tn_.length numeric entries)
  #
  def jb_visualize2d(an, tn)
    Visualizer::Table.new(an, tn, self).visualize
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
    Visualizer::Cube.new(an, bn, tn, self).visualize
  end
end


