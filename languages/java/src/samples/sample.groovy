import org.m3.hron.HronParser

def hronBlob = """\
@welcome
	=title
		Welcome to HRON
	=copy
		HRON is a new data format
		which is easy to read and
		supports multi line strings.
		
		Best,
		The Developers
	@authors
		=firstName
			Bob
		=lastName
			Developer
	@
		=firstName
			Steve
		=lastName
			Stevensson
"""
    
def hron = new HronParser().parseText(hronBlob)
    
assert hron instanceof Map 
assert hron.welcome.title == "Welcome to HRON"
assert hron.welcome.copy.readLines()[5] == "The Developers"
assert hron.welcome.author instanceof Map
assert hron.welcome.author.firstName == "Bob"

println "Hron sample successfully executed!"