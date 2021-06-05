# pd

"Java snippets."

"simple, fast, firm and secure"

"Often, good design is intuitive."

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/">
  <img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" />
</a>
<br/>
<br/>

## pd.json

Depends on pd.fenc, thus being a package rather than a sub project.

Converts `IJsonToken` <=> `String` via `JsonCodec`.

`IJsonToken` is extended by:
  - `IJsonNull`
  - `IJsonBoolean`
  - `IJsonInt`
  - `IJsonFloat`
  - `IJsonString`
  - `IJsonArray`
  - `IJsonTable`

To create a json token, use `JsonCodec.tokenFactory` variable.

**TODO** convert json token => java object thus somehow hide json token  
it is troublesome due to  
1. java type erasure
2. with interface type and instance known but the implementation type unknown

**TODO** notation as config

**TODO** custom serialize() & deserialize() on java object
