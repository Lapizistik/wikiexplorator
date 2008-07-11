#! /usr/bin/ruby -w

JBRIDGE_OPTIONS = { 
  :classpath => 
  %w[Visualizer.jar lib/prefuse.jar lib/forms-1.2.0.jar].collect { |p| 
    "Visualizer/#{p}" }.join(':')
}
require 'yajb/jbridge'
include JavaBridge
jimport 'visualizer.VisuMain'
jimport 'visualizer.ruby.FlatCube'

X = 60
Y = 60
Z = 300


$data = (1..X).collect { |i|
  (1..Y).collect { |k|
    if (i+k)%2 == 0
      (1..Z).to_a.collect { |i| i*100.0/Z }
    else
      (1..Z).to_a.collect { |i| 101.0-i*100.0/Z }
    end
  }
}
# puts $data.inspect

$xaxis_names = (1..X).collect { |i| "x#{i}" }
$yaxis_names = (1..Y).collect { |i| "y#{i}" }
$zaxis_names = (1..Z).collect { |i| "z#{i}" }

puts 'new VisMain()'
j = jnew('VisuMain')

puts 'FlatCube.init()'
fc = :FlatCube.jnew([:t_double] + $data.flatten, $xaxis_names,$yaxis_names,$zaxis_names)

puts 'VisuMain.init(FlatCube)'
j.init(fc)

stop_thread

