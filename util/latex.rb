#!/usr/bin/ruby -w
# :title: LaTeX - Ruby utility module
# = LaTeX Utility Module
#
# 

module LaTeX
  class << self # all module-functions
    # returns _a_ formated as LaTeX-table.
    #
    # _a_:: a two-dimensional array.
    #       Note that you can use <tt>a.transpose</tt> to flip 
    #       rows and columns.
    def table(a)
      width=0
      body = a.collect do |row|
        width=[width,row.length].max
        '    ' + row.join(' & ')
      end.join("\\\\\n")
      '  \begin{tabular}{' + 'l'*width + '}
' + body + '
  \end{tabular}
'
    end
    
    def histogram(h, &sortby)
      if sortby
        h = h.sort_by &sortby
      else
        h = h.sort
      end
      '  \begin{tabular}{ll}
' + h.collect { |k,v| "    \\balken{#{k}}{#{v}}" }.join("\\\\\n") + '
  \end{tabular}
'
    end
  end
end
