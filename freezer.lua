-- freezer
--
-- llllllll.co/t/freezer
--
-- freezer
-- v0
--
--    ▼ instructions below ▼
local started = false
local freezerValue = 0
function init()
    -- setup osc
    osc_fun = {
        freezeFader = function(args)
            freezerValue = tonumber(args[1])
            started = true
        end
    }
    osc.event = function(path, args, from)
        if string.sub(path, 1, 1) == "/" then path = string.sub(path, 2) end
        if path ~= nil and osc_fun[path] ~= nil then
            osc_fun[path](args)
        else
            -- print("osc.event: '"..path.."' ?")
        end
    end

    local params_menu = {
        {
            id = "db",
            name = "db",
            engine = true,
            min = -96,
            max = 16,
            exp = false,
            div = 1,
            default = -6,
            unit = "dB"
        }, {
            id = "freeze",
            name = "freeze",
            engine = true,
            min = 0,
            max = 1,
            div = 1,
            default = 0
        }, {
            id = "bufferDuration",
            name = "freeze time",
            engine = false,
            min = 0.1,
            max = 30.0,
            div = 0.1,
            default = 2.0
        }, {
            id = "freezeTime",
            name = "fade time",
            engine = true,
            min = 0.1,
            max = 30.0,
            div = 0.1,
            default = 10.0
        }, {
            id = "impulseFreq",
            name = "tremolo",
            engine = true,
            min = 0.5,
            max = 30.0,
            div = 0.5,
            default = 10.0,
            unit = "hz"
        }
    }

    for _, pram in ipairs(params_menu) do
        local formatter = pram.formatter
        if formatter == nil and pram.values ~= nil then
            formatter = function(param)
                return pram.values[param:get()] ..
                           (pram.unit and (" " .. pram.unit) or "")
            end
        end
        local pid = pram.id
        params:add{
            type = "control",
            id = pid,
            name = pram.name,
            controlspec = controlspec.new(pram.min, pram.max,
                                          pram.exp and "exp" or "lin", pram.div,
                                          pram.default, pram.unit or "",
                                          pram.div / (pram.max - pram.min)),
            formatter = formatter
        }
        if pram.hide then params:hide(pid) end
        params:set_action(pid, function(x)
            if pram.engine then
                engine.set(pram.id, x)
            elseif pram.action then
                pram.action(x)
            end
        end)
    end

end

function key(n, z)
    if n == 1 then
    elseif n == 2 then
    elseif n == 3 then
        engine.freezer(params:get("bufferDuration"))
    end
end

function enc(n, d)
    if n == 1 then
        params:delta("bufferDuration", d)
    elseif n == 2 then
        params:delta("freezeTime", d)
    elseif n == 3 then
        params:delta("impulseFreq", d)
    end
end

function refresh() redraw() end

function redraw()
    screen.clear()
    -- draw the current values on the screen
    screen.level(15)
    screen.move(10, 10)
    screen.text("bufferDuration: " .. params:string("bufferDuration"))
    screen.move(10, 20)
    screen.text("freezeTime: " .. params:string("freezeTime"))
    screen.move(10, 30)
    screen.text("impulseFreq: " .. params:string("impulseFreq"))
    screen.move(10, 40)
    screen.text("freezeValue: " .. freezerValue)
    screen.update()
end

