class Addtagged < ActiveRecord::Migration
  def self.up
    add_column :measures, :tagged, :boolean , :default=>false
  end

  def self.down
  end
end
