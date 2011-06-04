# --------------------------------------------------------------------------------
#  NoiseTube Web application
#  
#  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
#  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
# --------------------------------------------------------------------------------
#  This library is free software; you can redistribute it and/or modify it under
#  the terms of the GNU Lesser General Public License, version 2.1, as published
#  by the Free Software Foundation.
#  
#  This library is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
#  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
#  details.
#  
#  You should have received a copy of the GNU Lesser General Public License along
#  with this library; if not, write to:
#    Free Software Foundation, Inc.,
#    51 Franklin Street, Fifth Floor,
#    Boston, MA  02110-1301, USA.
#  
#  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
#  NoiseTube project source code repository: http://code.google.com/p/noisetube
# --------------------------------------------------------------------------------
#  More information:
#   - NoiseTube project website: http://www.noisetube.net
#   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
#   - VUB BrusSense team: http://www.brussense.be
# --------------------------------------------------------------------------------
 

# A Site key gives additional protection against a dictionary attack if your
# DB is ever compromised.  With no site key, we store
#   DB_password = hash(user_password, DB_user_salt)
# If your database were to be compromised you'd be vulnerable to a dictionary
# attack on all your stupid users' passwords.  With a site key, we store
#   DB_password = hash(user_password, DB_user_salt, Code_site_key)
# That means an attacker needs access to both your site's code *and* its
# database to mount an "offline dictionary attack.":http://www.dwheeler.com/secure-programs/Secure-Programs-HOWTO/web-authentication.html
# 
# It's probably of minor importance, but recommended by best practices: 'defense
# in depth'.  Needless to say, if you upload this to github or the youtubes or
# otherwise place it in public view you'll kinda defeat the point.  Your users'
# passwords are still secure, and the world won't end, but defense_in_depth -= 1.
# 
# Please note: if you change this, all the passwords will be invalidated, so DO
# keep it someplace secure.  Use the random value given or type in the lyrics to
# your favorite Jay-Z song or something; any moderately long, unpredictable text.
REST_AUTH_SITE_KEY         = 'CHANGE_THIS'
  
# Repeated applications of the hash make brute force (even with a compromised
# database and site key) harder, and scale with Moore's law.
#
#   bq. "To squeeze the most security out of a limited-entropy password or
#   passphrase, we can use two techniques [salting and stretching]... that are
#   so simple and obvious that they should be used in every password system.
#   There is really no excuse not to use them." http://tinyurl.com/37lb73
#   Practical Security (Ferguson & Scheier) p350
# 
# A modest 10 foldings (the default here) adds 3ms.  This makes brute forcing 10
# times harder, while reducing an app that otherwise serves 100 reqs/s to 78 signin
# reqs/s, an app that does 10reqs/s to 9.7 reqs/s
# 
# More:
# * http://www.owasp.org/index.php/Hashing_Java
# * "An Illustrated Guide to Cryptographic Hashes":http://www.unixwiz.net/techtips/iguide-crypto-hashes.html

REST_AUTH_DIGEST_STRETCHES = 10
