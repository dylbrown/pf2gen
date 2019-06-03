<!DOCTYPE html>
<html>
<head>
    <title>Output</title>
</head>
<body>
<h3>${character.name}</h3>
    <p>
        <b>${character.pclass.name} ${character.level}</b><br />
        <b>Perception</b> ${attributes.perception?string.@s}<br />
        <b>Languages</b>
        <#list character.languages as language>
            ${language}<#sep>, </#sep>
        <#else>None</#list>
        <br />
        <b>Skills</b>
        <#list skills as skill>
            ${skill.name} ${skill.mod?string.@s}<#sep>, </#sep>
        </#list>
        <br />
        <b>Str</b> ${abilityMod.str}, <b>Dex</b> ${abilityMod.dex}, <b>Con</b> ${abilityMod.con}, <b>Int</b> ${abilityMod.con}, <b>Wis</b> ${abilityMod.wis}, <b>Cha</b> ${abilityMod.cha}<br />
        <b>Items</b>
        <#list items as item>
            ${item.count} ${item.name}<#sep>, </#sep>
        <#else>None</#list>
        <hr />
        <b>AC</b> ${character.ac}, <b>TAC</b> ${character.tac}; <b>Fort</b> ${attributes.fortitude?string.@s}, <b>Ref</b> ${attributes.reflex?string.@s}, <b>Will</b> ${attributes.will?string.@s}<br />
        <b>HP</b> ${character.hp}<br />
<b>Speed</b> ${character.speed} feet<br />
        <hr />
	<#assign count=0>
    <#list items as item>
        <#if classes.weapon.isInstance(item)>
			<#assign count++>
            <#if item.isRanged()>
                <b>① Ranged</b>
            <#else>
                <b>① Melee</b>
            </#if>
            ${item.name} ${character.getAttackMod(item)?string.@s}
            <#list item.traits>(<#items as weaponTrait>${weaponTrait.name}<#sep>, </#sep></#items>)</#list>
			, <b>Damage</b> ${item.damage}<#if character.getDamageMod(item) != 0>${character.getDamageMod(item)?string.@s}</#if>
			${item.damagetype}
			<#sep><br></#sep>
        </#if>
	</#list>
	<#if count gt 0><hr /></#if>
	<#list abilities as ability>
		<p>
		<b>${ability.name}</b><br>
		<#assign reaction=false>
		<#if classes.activity.isInstance(ability)><b><#switch ability.cost>
			<#case "Free">Ⓕ <#break>
			<#case "Reaction">Ⓡ <#assign reaction=true><#break>
			<#case "One">① <#break>
			<#case "Two">② <#break>
			<#case "Three">③ <#break>
		</#switch> </b> </#if>${ability.desc}
		<#if reaction>
			<br><b>Trigger</b> ${ability.trigger}
		</#if>
		</p>
	</#list>
    </p>
</body>
</html>