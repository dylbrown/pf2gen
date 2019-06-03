<!DOCTYPE html>
<html>
<head>
    <title>Output</title>
</head>
<body>
<h3>${character.name}</h3>
    <p>
        <b>${character.pclass.name} ${character.level}</b><br />
        <b>Perception</b> ${attributes.perception}<br />
        <b>Languages</b><br />
        <b>Skills</b> %s<br />
        <b>Str</b> ${abilityMod.str}, <b>Dex</b> ${abilityMod.dex}, <b>Con</b> ${abilityMod.con}, <b>Int</b> ${abilityMod.con}, <b>Wis</b> ${abilityMod.wis}, <b>Cha</b> ${abilityMod.cha}<br />
        <b>Items</b>
        <#list items as item>
            ${item.count} ${item.name}<#sep>, </#sep>
        </#list>
        <hr />
        <b>AC</b> ${character.ac}, <b>TAC</b> ${character.tac}; <b>Fort</b> ${attributes.fortitude}, <b>Ref</b> ${attributes.reflex}, <b>Will</b> ${attributes.will}<br />
        <b>HP</b> ${character.hp}<br />
        <hr />
        <b>Speed</b> ${character.speed} feet<br />
    </p>
</body>
</html>