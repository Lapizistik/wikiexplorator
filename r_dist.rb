require 'wio'

RSRuby.instance.library('igraph')
R = RSRuby.instance

def to_igraph(graph)
  ni = Hash.new
  graph.nodes.each_with_index { |n,i| ni[n]=i }
  list = []
  graph.links.each_value do |l|
    a = ni[l.src]
    b = ni[l.dest]
    list << a << b
#    list << b << a
  end
  list
  R.graph(list, :directed => true)
end

def diameter(graph)
  RSRuby.set_default_mode(RSRuby::NO_CONVERSION)
#  r = graph.to_r_matrix
#  r = R.graph_adjacency(r)
  r = to_igraph(graph)
  RSRuby.set_default_mode(RSRuby::BASIC_CONVERSION)
  R.diameter(r, :directed => true, :unconnected => true)
end

def dias(filename)
  w = Mediawiki::Wiki.marshal_load(filename)
  w.filter.namespace=0
  w.filter.deny_user(0,1)
  g = w.copagesgraph
  puts "[#{filename}] Copagesdiameter = #{diameter(g)}"
  g = w.coauthorgraph
  puts "[#{filename}] Coauthorsdiameter = #{diameter(g)}"
end

