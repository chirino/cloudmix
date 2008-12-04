# evalstring.rb

module EvalStringHelper
  # call-seq:
  #    evalstring( text )    => html
  #
  # Evaluates the string as an interpolated String
  #
  def evalstring( text )
    # p "called evalstring with '#{text}'"
    if text != nil 
      eval('"' + text + '"')
    else
      ''
    end
  end
end  # module EvalStringHelper

Webby::Helpers.register(EvalStringHelper)

# EOF
