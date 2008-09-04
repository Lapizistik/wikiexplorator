# :call-seq:
# try_to_require(<i>package</i>)
# try_to_require(<i>package</i>, <tt>:silent</tt>)
# try_to_require(<i>package</i>, 'some text', 'more text', ...)
#
# tries to require _package_. 
#
# Will return _true_ on success and _false_ otherwise.
#
# Unless <tt>:silent</tt> is given as first parameter a warning is given
# if the require fails. The warning consists of the the text of the LoadError
# and any strings given as parameters (displayed line by line).
def try_to_require(package, *args)
  begin
    return require(package)
  rescue LoadError => e
    return false if args.first==:silent
    warn "Require: #{e}"
    args.each do |line|
      warn "         " + line
    end
    return false
  end
end
