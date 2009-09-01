(TeX-add-style-hook "usage"
 (lambda ()
    (LaTeX-add-environments
     "typed"
     "File")
    (LaTeX-add-labels
     "sec:install"
     "sec:first"
     "sec:start"
     "sec:interactive"
     "typed:pu"
     "fig:gp_hist"
     "sec:gnuplot"
     "fig:gp_lorenz"
     "fig:gp_example"
     "fnt:pspdf"
     "fig:hyperlink"
     "fig:coauthor"
     "sec:network"
     "sec:dotgraphintro"
     "typed:remove_lonely_nodes"
     "sec:Rintro"
     "sec:gvis"
     "fig:gv-cadot"
     "fig:gv-cafdp"
     "fig:gv-cacirco"
     "fig:gv-catwopi"
     "fig:gv-caneato"
     "sec:graphviz"
     "fig:gv-catwopi2"
     "fig:gv-catwopi3"
     "fig:gv-catwopi4"
     "fig:gv-catwopi5"
     "sec:todo")
    (TeX-add-symbols
     '("rdoc" 1)
     '("cmd" 1)
     '("code" 1)
     '("file" 1)
     '("textrule" 1)
     "closerule"
     "tcount"
     "prompt"
     "cursor"
     "p")
    (TeX-run-style-hooks
     "hyperref"
     "colorlinks"
     "inputenc"
     "utf8"
     "framed"
     "graphicx"
     "xcolor"
     "alltt"
     "latex2e"
     "scrartcl10"
     "scrartcl"
     "a4paper")))

